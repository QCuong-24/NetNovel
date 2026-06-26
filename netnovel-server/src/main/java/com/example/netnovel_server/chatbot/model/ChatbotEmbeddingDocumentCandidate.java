package com.example.netnovel_server.chatbot.model;

import java.util.Map;

public record ChatbotEmbeddingDocumentCandidate(
    String documentType,
    String sourceId,
    String language,
    String content,
    String contentHash,
    Map<String, Object> metadata
) {
}
