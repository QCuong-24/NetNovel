package com.example.netnovel_server.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatbotEmbeddingStatusDTO {

    private boolean enabled;

    private String model;

    private int dimension;

    private long totalDocuments;

    private long activeDocuments;

    private long faqDocuments;

    private long intentDocuments;

    private LocalDateTime lastIndexedAt;
}
