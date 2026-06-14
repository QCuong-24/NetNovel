package com.example.netnovel_server.search.elastic.service;

import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Status;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.SearchUnavailableException;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.repository.NovelRepository;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
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

    private final RestClient restClient;
    private final NovelRepository novelRepository;
    private final ElasticNovelIndexManager indexManager;
    private final ObjectMapper objectMapper;

    public ElasticAdminNovelSearchService(
        RestClient restClient,
        NovelRepository novelRepository,
        ElasticNovelIndexManager indexManager
    ) {
        this.restClient = restClient;
        this.novelRepository = novelRepository;
        this.indexManager = indexManager;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional(readOnly = true)
    public Page<NovelSearchResultDTO> searchNovels(
        String query,
        String status,
        String genre,
        String tag,
        String source,
        Boolean crawled,
        Pageable pageable
    ) {
        indexManager.ensureNovelIndex();
        Map<String, Object> response = performSearch(query, status, genre, tag, source, crawled, pageable);
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

    private Map<String, Object> performSearch(
        String query,
        String status,
        String genre,
        String tag,
        String source,
        Boolean crawled,
        Pageable pageable
    ) {
        Request request = new Request("POST", "/" + indexManager.getNovelIndexName() + "/_search");
        request.setJsonEntity(searchRequestJson(query, status, genre, tag, source, crawled, pageable));
        try {
            String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
            return objectMapper.readValue(responseBody, Map.class);
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not search Elasticsearch novel index", exception);
        }
    }

    private String searchRequestJson(
        String query,
        String status,
        String genre,
        String tag,
        String source,
        Boolean crawled,
        Pageable pageable
    ) {
        String normalizedQuery = normalize(query);
        String normalizedStatus = normalizeStatus(status);
        String normalizedGenre = normalize(genre);
        String normalizedTag = normalize(tag);
        String normalizedSource = normalize(source);

        List<String> filters = new ArrayList<>();
        if (!normalizedStatus.isBlank()) {
            filters.add(termFilter("status", normalizedStatus));
        }
        if (!normalizedGenre.isBlank()) {
            filters.add(termFilter("genres", normalizedGenre));
        }
        if (!normalizedTag.isBlank()) {
            filters.add(termFilter("tags", normalizedTag));
        }
        if (!normalizedSource.isBlank()) {
            filters.add(termFilter("sourceName", normalizedSource));
        }
        if (crawled != null) {
            filters.add("""
                { "term": { "crawled": %s } }
                """.formatted(crawled));
        }

        String queryJson = normalizedQuery.isBlank()
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

    private String jsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize Elasticsearch query value", exception);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
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
