package com.example.netnovel_server.search.elastic.service;

import com.example.netnovel_server.dto.ElasticDiagnosticsBucketDTO;
import com.example.netnovel_server.dto.ElasticDiagnosticsResponseDTO;
import com.example.netnovel_server.exception.SearchUnavailableException;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

@Service
@ConditionalOnProperty(prefix = "app.search.elastic", name = "enabled", havingValue = "true")
public class ElasticDiagnosticsService {

    private static final List<String> EXACT_FIELDS = List.of("status", "genres", "tags", "sourceName");

    private final RestClient restClient;
    private final ElasticNovelIndexManager indexManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ElasticDiagnosticsService(RestClient restClient, ElasticNovelIndexManager indexManager) {
        this.restClient = restClient;
        this.indexManager = indexManager;
    }

    public ElasticDiagnosticsResponseDTO diagnostics() {
        boolean exists = indexManager.indexExists();
        if (!exists) {
            return ElasticDiagnosticsResponseDTO.builder()
                .enabled(true)
                .indexName(indexManager.getNovelIndexName())
                .exists(false)
                .documentCount(0)
                .embeddingDocumentCount(0)
                .mappingVersion("missing")
                .fieldMappings(Map.of())
                .statusBuckets(List.of())
                .topGenres(List.of())
                .topTags(List.of())
                .crawledBuckets(List.of())
                .embeddingModelBuckets(List.of())
                .build();
        }

        Map<String, String> fieldMappings = fieldMappings();
        return ElasticDiagnosticsResponseDTO.builder()
            .enabled(true)
            .indexName(indexManager.getNovelIndexName())
            .exists(true)
            .documentCount(count())
            .embeddingDocumentCount(countEmbeddings())
            .mappingVersion(mappingVersion(fieldMappings))
            .fieldMappings(fieldMappings)
            .statusBuckets(aggregation("status"))
            .topGenres(aggregation("genres"))
            .topTags(aggregation("tags"))
            .crawledBuckets(aggregation("crawled"))
            .embeddingModelBuckets(aggregation("embeddingModel"))
            .build();
    }

    private long count() {
        Map<String, Object> response = performJson("GET", "/" + indexManager.getNovelIndexName() + "/_count", null);
        Object count = response.get("count");
        return count instanceof Number number ? number.longValue() : 0L;
    }

    private Map<String, String> fieldMappings() {
        Map<String, Object> response = performJson("GET", "/" + indexManager.getNovelIndexName() + "/_mapping", null);
        Map<String, Object> index = asMap(response.get(indexManager.getNovelIndexName()));
        Map<String, Object> mappings = asMap(index.get("mappings"));
        Map<String, Object> properties = asMap(mappings.get("properties"));

        Map<String, String> result = new LinkedHashMap<>();
        for (String field : EXACT_FIELDS) {
            result.put(field, describeMapping(asMap(properties.get(field))));
        }
        result.put("crawled", describeMapping(asMap(properties.get("crawled"))));
        result.put("embeddingVector", describeVectorMapping(asMap(properties.get("embeddingVector"))));
        result.put("embeddingModel", describeMapping(asMap(properties.get("embeddingModel"))));
        result.put("embeddingDimension", describeMapping(asMap(properties.get("embeddingDimension"))));
        return result;
    }

    private List<ElasticDiagnosticsBucketDTO> aggregation(String field) {
        String aggregationField = field.equals("crawled") ? field : exactAggregationField(field);
        String body = """
            {
              "size": 0,
              "aggs": {
                "values": {
                  "terms": {
                    "field": %s,
                    "size": 20
                  }
                }
              }
            }
            """.formatted(jsonString(aggregationField));
        Map<String, Object> response = performJson("POST", "/" + indexManager.getNovelIndexName() + "/_search", body);
        Map<String, Object> aggregations = asMap(response.get("aggregations"));
        Map<String, Object> values = asMap(aggregations.get("values"));
        return asList(values.get("buckets")).stream()
            .map(bucket -> ElasticDiagnosticsBucketDTO.builder()
                .key(String.valueOf(bucket.get("key")))
                .count(asLong(bucket.get("doc_count")))
                .build())
            .toList();
    }

    private String exactAggregationField(String field) {
        String mapping = fieldMappings().get(field);
        return mapping.contains("text+keyword") ? field + ".keyword" : field;
    }

    private String describeMapping(Map<String, Object> mapping) {
        String type = String.valueOf(mapping.getOrDefault("type", "missing"));
        Map<String, Object> fields = asMap(mapping.get("fields"));
        if (fields.containsKey("keyword")) {
            return type + "+keyword";
        }
        return type;
    }

    private String describeVectorMapping(Map<String, Object> mapping) {
        if (mapping.isEmpty()) {
            return "missing";
        }
        return String.valueOf(mapping.getOrDefault("type", "missing"))
            + "[dims=" + mapping.getOrDefault("dims", "?") + "]";
    }

    private String mappingVersion(Map<String, String> mappings) {
        boolean legacy = mappings.values().stream().anyMatch(value -> value.contains("text+keyword"));
        boolean standard = EXACT_FIELDS.stream().allMatch(field -> "keyword".equals(mappings.get(field)));
        if (standard) {
            return "standard_keyword";
        }
        if (legacy) {
            return "legacy_text_keyword";
        }
        return "custom_or_dynamic";
    }

    private long countEmbeddings() {
        String body = """
            {
              "query": {
                "exists": {
                  "field": "embeddingVector"
                }
              }
            }
            """;
        Map<String, Object> response = performJson("GET", "/" + indexManager.getNovelIndexName() + "/_count", body);
        Object count = response.get("count");
        return count instanceof Number number ? number.longValue() : 0L;
    }

    private Map<String, Object> performJson(String method, String endpoint, String body) {
        Request request = new Request(method, endpoint);
        if (body != null) {
            request.setJsonEntity(body);
        }
        try {
            String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
            return objectMapper.readValue(responseBody, Map.class);
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not read Elasticsearch diagnostics", exception);
        }
    }

    private String jsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize Elasticsearch diagnostics value", exception);
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

    private long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}
