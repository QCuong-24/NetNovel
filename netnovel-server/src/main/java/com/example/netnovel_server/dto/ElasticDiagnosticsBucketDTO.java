package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElasticDiagnosticsBucketDTO {

    private String key;

    private long count;
}
