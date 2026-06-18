package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElasticReindexResponseDTO {

    private String indexName;

    private int indexed;

    private int failed;
}
