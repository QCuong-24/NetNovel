package com.example.netnovel_server.chatbot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EmbeddingResponseDTO {

    private String model;

    private int dimension;

    private List<List<Double>> embeddings;
}
