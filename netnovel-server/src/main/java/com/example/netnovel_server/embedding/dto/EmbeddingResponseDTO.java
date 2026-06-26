package com.example.netnovel_server.embedding.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbeddingResponseDTO {

    private String model;

    private int dimension;

    private List<List<Double>> embeddings;
}
