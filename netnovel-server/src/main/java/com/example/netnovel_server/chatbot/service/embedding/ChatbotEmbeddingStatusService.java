package com.example.netnovel_server.chatbot.service.embedding;

import com.example.netnovel_server.chatbot.config.ChatbotEmbeddingProperties;
import com.example.netnovel_server.chatbot.dto.ChatbotEmbeddingStatusDTO;
import com.example.netnovel_server.chatbot.repository.ChatbotEmbeddingDocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatbotEmbeddingStatusService {

    private final ChatbotEmbeddingProperties properties;
    private final ChatbotEmbeddingDocumentRepository repository;

    public ChatbotEmbeddingStatusService(
        ChatbotEmbeddingProperties properties,
        ChatbotEmbeddingDocumentRepository repository
    ) {
        this.properties = properties;
        this.repository = repository;
    }

    public ChatbotEmbeddingStatusDTO status() {
        if (!repository.tableExists()) {
            return emptyStatus();
        }

        ChatbotEmbeddingStatusDTO status = repository.status(properties.model());
        return ChatbotEmbeddingStatusDTO.builder()
            .enabled(properties.enabled())
            .model(properties.model())
            .dimension(properties.dimension())
            .totalDocuments(status.getTotalDocuments())
            .activeDocuments(status.getActiveDocuments())
            .faqDocuments(status.getFaqDocuments())
            .intentDocuments(status.getIntentDocuments())
            .lastIndexedAt(status.getLastIndexedAt())
            .build();
    }

    private ChatbotEmbeddingStatusDTO emptyStatus() {
        return ChatbotEmbeddingStatusDTO.builder()
            .enabled(properties.enabled())
            .model(properties.model())
            .dimension(properties.dimension())
            .totalDocuments(0)
            .activeDocuments(0)
            .faqDocuments(0)
            .intentDocuments(0)
            .build();
    }
}
