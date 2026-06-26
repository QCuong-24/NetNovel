package com.example.netnovel_server.chatbot.service.embedding;

import com.example.netnovel_server.chatbot.config.ChatbotEmbeddingProperties;
import com.example.netnovel_server.chatbot.repository.ChatbotEmbeddingDocumentRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ChatbotEmbeddingSchemaInitializer implements ApplicationRunner {

    private final ChatbotEmbeddingProperties properties;
    private final ChatbotEmbeddingDocumentRepository repository;

    public ChatbotEmbeddingSchemaInitializer(
        ChatbotEmbeddingProperties properties,
        ChatbotEmbeddingDocumentRepository repository
    ) {
        this.properties = properties;
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.enabled()) {
            repository.initializeSchema();
        }
    }
}

