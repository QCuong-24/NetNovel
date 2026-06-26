package com.example.netnovel_server.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElasticDiagnosticsResponseDTO {

    private boolean enabled;

    private String indexName;

    private boolean exists;

    private long documentCount;

    private long embeddingDocumentCount;

    private String mappingVersion;

    private Map<String, String> fieldMappings;

    private List<ElasticDiagnosticsBucketDTO> statusBuckets;

    private List<ElasticDiagnosticsBucketDTO> topGenres;

    private List<ElasticDiagnosticsBucketDTO> topTags;

    private List<ElasticDiagnosticsBucketDTO> crawledBuckets;

    private List<ElasticDiagnosticsBucketDTO> embeddingModelBuckets;
}
