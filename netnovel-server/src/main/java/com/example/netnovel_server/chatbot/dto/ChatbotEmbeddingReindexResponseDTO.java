package com.example.netnovel_server.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatbotEmbeddingReindexResponseDTO {

    private boolean enabled;

    private String model;

    private int dimension;

    private int documents;

    private int batches;

    private String message;
}
