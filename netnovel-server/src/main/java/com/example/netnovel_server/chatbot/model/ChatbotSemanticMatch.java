package com.example.netnovel_server.chatbot.model;

public record ChatbotSemanticMatch(
    long id,
    String documentType,
    String sourceId,
    String language,
    String content,
    String metadataJson,
    double similarity
) {
}
