package com.example.netnovel_server.search.elastic.service;

import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.embedding.config.NovelEmbeddingProperties;
import com.example.netnovel_server.embedding.service.NovelEmbeddingClient;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.SearchUnavailableException;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.repository.NovelRepository;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.ResponseException;
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
public class ElasticSemanticNovelSearchService {

    private final RestClient restClient;
    private final NovelRepository novelRepository;
    private final ElasticNovelIndexManager indexManager;
    private final NovelEmbeddingProperties embeddingProperties;
    private final NovelEmbeddingClient embeddingClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ElasticSemanticNovelSearchService(
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
    }

    @Transactional(readOnly = true)
    public Page<NovelSearchResultDTO> semanticSearch(String query, Pageable pageable) {
        ensureEnabled();
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isBlank()) {
            throw new BadRequestException("Semantic search query must not be blank");
        }

        List<Double> queryVector = embeddingClient.embedQuery(normalizedQuery);
        if (queryVector.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        return searchByVector(queryVector, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelSearchResultDTO> similarNovels(Long novelId, Pageable pageable) {
        ensureEnabled();
        List<Double> vector = getNovelVector(novelId);
        if (vector.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        return searchByVector(vector, novelId, pageable);
    }

    private Page<NovelSearchResultDTO> searchByVector(List<Double> vector, Long excludedNovelId, Pageable pageable) {
        indexManager.ensureNovelIndex();
        Map<String, Object> response = performSearch(vector, excludedNovelId, pageable);
        Map<String, Object> hitsWrapper = asMap(response.get("hits"));
        List<Map<String, Object>> hits = asList(hitsWrapper.get("hits"));
        long total = totalHits(hitsWrapper.get("total"));

        List<Long> novelIds = hits.stream()
            .map(hit -> asMap(hit.get("_source")))
            .map(source -> asLong(source.get("novelId")))
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

    private Map<String, Object> performSearch(List<Double> vector, Long excludedNovelId, Pageable pageable) {
        Request request = new Request("POST", "/" + indexManager.getNovelIndexName() + "/_search");
        request.setJsonEntity(vectorSearchRequestJson(vector, excludedNovelId, pageable));
        try {
            String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
            return objectMapper.readValue(responseBody, Map.class);
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not run semantic Elasticsearch novel search", exception);
        }
    }

    private String vectorSearchRequestJson(List<Double> vector, Long excludedNovelId, Pageable pageable) {
        int pageSize = Math.max(1, pageable.getPageSize());
        int from = Math.toIntExact(pageable.getOffset());
        List<String> filters = new ArrayList<>();
        filters.add("""
            { "exists": { "field": "embeddingVector" } }
            """);
        if (excludedNovelId != null) {
            filters.add("""
                { "bool": { "must_not": [{ "term": { "novelId": %d } }] } }
                """.formatted(excludedNovelId));
        }

        return """
            {
              "from": %d,
              "size": %d,
              "min_score": %.6f,
              "_source": ["novelId"],
              "query": {
                "script_score": {
                  "query": {
                    "bool": {
                      "filter": [%s]
                    }
                  },
                  "script": {
                    "source": "cosineSimilarity(params.queryVector, 'embeddingVector') + 1.0",
                    "params": {
                      "queryVector": %s
                    }
                  }
                }
              }
            }
            """.formatted(
            from,
            pageSize,
            1.0 + embeddingProperties.minSimilarity(),
            String.join(",", filters),
            jsonArray(vector)
        );
    }

    private List<Double> getNovelVector(Long novelId) {
        Request request = new Request("GET", "/" + indexManager.getNovelIndexName() + "/_doc/" + novelId);
        try {
            String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            return asDoubleList(asMap(response.get("_source")).get("embeddingVector"));
        } catch (ResponseException exception) {
            if (exception.getResponse().getStatusLine().getStatusCode() == 404) {
                return List.of();
            }
            throw new SearchUnavailableException("Could not read Elasticsearch novel vector: " + novelId, exception);
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not read Elasticsearch novel vector: " + novelId, exception);
        }
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

    private void ensureEnabled() {
        if (!embeddingProperties.enabled()) {
            throw new SearchUnavailableException("Novel embedding search is disabled");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private String jsonArray(List<Double> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize Elasticsearch vector query values", exception);
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

    private List<Double> asDoubleList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Double> result = new ArrayList<>(list.size());
        for (Object item : list) {
            if (item instanceof Number number) {
                result.add(number.doubleValue());
            }
        }
        return result;
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
