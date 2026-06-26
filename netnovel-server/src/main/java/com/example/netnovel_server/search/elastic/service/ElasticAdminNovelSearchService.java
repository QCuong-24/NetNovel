package com.example.netnovel_server.search.elastic.service;

import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.embedding.config.NovelEmbeddingProperties;
import com.example.netnovel_server.embedding.service.NovelEmbeddingClient;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Status;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.SearchUnavailableException;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.repository.NovelRepository;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(prefix = "app.search.elastic", name = "enabled", havingValue = "true")
public class ElasticAdminNovelSearchService {

    private static final Logger log = LoggerFactory.getLogger(ElasticAdminNovelSearchService.class);

    private static final double HYBRID_KEYWORD_WEIGHT = 0.65;
    private static final double HYBRID_SEMANTIC_WEIGHT = 0.35;

    private final RestClient restClient;
    private final NovelRepository novelRepository;
    private final ElasticNovelIndexManager indexManager;
    private final NovelEmbeddingProperties embeddingProperties;
    private final NovelEmbeddingClient embeddingClient;
    private final ObjectMapper objectMapper;

    public ElasticAdminNovelSearchService(
        RestClient restClient,
        NovelRepository novelRepository,
        ElasticNovelIndexManager indexManager,
        NovelEmbeddingProperties embeddingProperties,
        NovelEmbeddingClient embeddingClient
    ) {
        this.restClient = restClient;
        this.novelRepository = novelRepository;
        this.indexManager = indexManager;
        this.embeddingProperties = embeddingProperties;
        this.embeddingClient = embeddingClient;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional(readOnly = true)
    public Page<NovelSearchResultDTO> searchNovels(
        String query,
        String status,
        List<String> genres,
        List<String> tags,
        String source,
        Boolean crawled,
        Pageable pageable
    ) {
        indexManager.ensureNovelIndex();
        Map<String, Object> response = performSearchWithFallback(query, status, genres, tags, source, crawled, pageable);
        Map<String, Object> hitsWrapper = asMap(response.get("hits"));
        List<Map<String, Object>> hits = asList(hitsWrapper.get("hits"));
        long total = totalHits(hitsWrapper.get("total"));

        List<Long> novelIds = hits.stream()
            .map(hit -> asMap(hit.get("_source")))
            .map(sourceMap -> asLong(sourceMap.get("novelId")))
            .filter(Objects::nonNull)
            .toList();
        Map<Long, Novel> novelsById = novelRepository.findAllById(novelIds).stream()
            .collect(Collectors.toMap(Novel::getId, Function.identity()));

        List<NovelSearchResultDTO> results = hits.stream()
            .map(hit -> toResult(hit, novelsById))
            .filter(Objects::nonNull)
            .toList();

        return new PageImpl<>(results, pageable, total);
    }

    private Map<String, Object> performSearchWithFallback(
        String query,
        String status,
        List<String> genres,
        List<String> tags,
        String source,
        Boolean crawled,
        Pageable pageable
    ) {
        if (!canUseHybridSearch(query)) {
            return performSearch(query, status, genres, tags, source, crawled, pageable);
        }

        try {
            List<Double> queryVector = embeddingClient.embedQuery(normalize(query));
            if (queryVector.isEmpty()) {
                return performSearch(query, status, genres, tags, source, crawled, pageable);
            }
            Map<String, Object> response = performHybridSearch(query, queryVector, status, genres, tags, source, crawled, pageable);
            if (totalHits(asMap(response.get("hits")).get("total")) == 0) {
                return performSearch(query, status, genres, tags, source, crawled, pageable);
            }
            return response;
        } catch (BadRequestException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("Hybrid novel search failed; falling back to keyword Elasticsearch search", exception);
            return performSearch(query, status, genres, tags, source, crawled, pageable);
        }
    }

    private boolean canUseHybridSearch(String query) {
        return embeddingProperties.enabled() && !normalize(query).isBlank();
    }

    private Map<String, Object> performSearch(
        String query,
        String status,
        List<String> genres,
        List<String> tags,
        String source,
        Boolean crawled,
        Pageable pageable
    ) {
        Request request = new Request("POST", "/" + indexManager.getNovelIndexName() + "/_search");
        request.setJsonEntity(searchRequestJson(query, status, genres, tags, source, crawled, pageable));
        try {
            String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
            return objectMapper.readValue(responseBody, Map.class);
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not search Elasticsearch novel index", exception);
        }
    }

    private Map<String, Object> performHybridSearch(
        String query,
        List<Double> queryVector,
        String status,
        List<String> genres,
        List<String> tags,
        String source,
        Boolean crawled,
        Pageable pageable
    ) {
        Request request = new Request("POST", "/" + indexManager.getNovelIndexName() + "/_search");
        request.setJsonEntity(hybridSearchRequestJson(query, queryVector, status, genres, tags, source, crawled, pageable));
        try {
            String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
            return objectMapper.readValue(responseBody, Map.class);
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not run hybrid Elasticsearch novel search", exception);
        }
    }

    private String searchRequestJson(
        String query,
        String status,
        List<String> genres,
        List<String> tags,
        String source,
        Boolean crawled,
        Pageable pageable
    ) {
        String normalizedQuery = normalize(query);
        List<String> filters = filters(status, genres, tags, source, crawled);
        String queryJson = keywordQueryJson(normalizedQuery);

        int pageSize = Math.max(1, pageable.getPageSize());
        int from = Math.toIntExact(pageable.getOffset());

        return """
            {
              "from": %d,
              "size": %d,
              "query": {
                "bool": {
                  "must": [%s],
                  "filter": [%s]
                }
              },
              "sort": [
                { "_score": "desc" },
                { "popularityScore": "desc" },
                { "freshnessScore": "desc" },
                { "updatedAt": "desc" }
              ]
            }
            """.formatted(from, pageSize, queryJson, String.join(",", filters));
    }

    private String hybridSearchRequestJson(
        String query,
        List<Double> queryVector,
        String status,
        List<String> genres,
        List<String> tags,
        String source,
        Boolean crawled,
        Pageable pageable
    ) {
        String normalizedQuery = normalize(query);
        List<String> filters = new ArrayList<>(filters(status, genres, tags, source, crawled));
        filters.add("""
            { "exists": { "field": "embeddingVector" } }
            """);

        int pageSize = Math.max(1, pageable.getPageSize());
        int from = Math.toIntExact(pageable.getOffset());

        return """
            {
              "from": %d,
              "size": %d,
              "track_total_hits": true,
              "_source": ["novelId"],
              "query": {
                "script_score": {
                  "query": {
                    "bool": {
                      "should": [%s],
                      "filter": [%s]
                    }
                  },
                  "script": {
                    "source": "double semanticScore = cosineSimilarity(params.queryVector, 'embeddingVector') + 1.0; return (_score * params.keywordWeight) + (semanticScore * params.semanticWeight);",
                    "params": {
                      "queryVector": %s,
                      "keywordWeight": %.4f,
                      "semanticWeight": %.4f
                    }
                  }
                }
              }
            }
            """.formatted(
            from,
            pageSize,
            keywordQueryJson(normalizedQuery),
            String.join(",", filters),
            jsonArray(queryVector),
            HYBRID_KEYWORD_WEIGHT,
            HYBRID_SEMANTIC_WEIGHT
        );
    }

    private List<String> filters(
        String status,
        List<String> genres,
        List<String> tags,
        String source,
        Boolean crawled
    ) {
        String normalizedStatus = normalizeStatus(status);
        List<String> normalizedGenres = normalizeList(genres);
        List<String> normalizedTags = normalizeList(tags);
        String normalizedSource = normalize(source);

        List<String> filters = new ArrayList<>();
        if (!normalizedStatus.isBlank()) {
            filters.add(exactTermFilter("status", normalizedStatus));
        }
        if (!normalizedGenres.isEmpty()) {
            filters.addAll(normalizedGenres.stream()
                .map(genre -> exactTermFilter("genres", genre))
                .toList());
        }
        if (!normalizedTags.isEmpty()) {
            filters.add(exactTermsFilter("tags", normalizedTags));
        }
        if (!normalizedSource.isBlank()) {
            filters.add(exactTermFilter("sourceName", normalizedSource));
        }
        if (crawled != null) {
            filters.add("""
                { "term": { "crawled": %s } }
                """.formatted(crawled));
        }
        return filters;
    }

    private String keywordQueryJson(String normalizedQuery) {
        return normalizedQuery.isBlank()
            ? """
                { "match_all": {} }
                """
            : """
                {
                  "multi_match": {
                    "query": %s,
                    "fields": [
                      "title^5",
                      "title.suggest^3",
                      "author^3",
                      "author.suggest^2",
                      "genres^2",
                      "tags^2",
                      "description",
                      "recommendationText"
                    ],
                    "fuzziness": "AUTO"
                  }
                }
                """.formatted(jsonString(normalizedQuery));
    }

    private NovelSearchResultDTO toResult(Map<String, Object> hit, Map<Long, Novel> novelsById) {
        Map<String, Object> source = asMap(hit.get("_source"));
        Long novelId = asLong(source.get("novelId"));
        Novel novel = novelsById.get(novelId);
        if (novel == null) {
            return null;
        }

        return NovelSearchResultDTO.builder()
            .novel(NovelMapper.toDTO(novel))
            .score(asDouble(hit.get("_score")))
            .build();
    }

    private String termFilter(String field, String value) {
        return """
            { "term": { "%s": %s } }
            """.formatted(field, jsonString(value));
    }

    private String termsFilter(String field, List<String> values) {
        return """
            { "terms": { "%s": %s } }
            """.formatted(field, jsonArray(values));
    }

    private String exactTermFilter(String field, String value) {
        return """
            {
              "bool": {
                "should": [
                  %s,
                  %s
                ],
                "minimum_should_match": 1
              }
            }
            """.formatted(termFilter(field, value), termFilter(field + ".keyword", value));
    }

    private String exactTermsFilter(String field, List<String> values) {
        return """
            {
              "bool": {
                "should": [
                  %s,
                  %s
                ],
                "minimum_should_match": 1
              }
            }
            """.formatted(termsFilter(field, values), termsFilter(field + ".keyword", values));
    }

    private String jsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize Elasticsearch query value", exception);
        }
    }

    private String jsonArray(List<?> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize Elasticsearch query values", exception);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
            .filter(Objects::nonNull)
            .flatMap(value -> Arrays.stream(value.split(",")))
            .map(this::normalize)
            .filter(value -> !value.isBlank())
            .distinct()
            .toList();
    }

    private String normalizeStatus(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return "";
        }

        try {
            return Status.valueOf(normalized.toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid novel status: " + value);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asList(Object value) {
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    private long totalHits(Object value) {
        Map<String, Object> total = asMap(value);
        Long count = asLong(total.get("value"));
        return count == null ? 0 : count;
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.valueOf(text);
        }
        return null;
    }

    private Double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return null;
    }
}
