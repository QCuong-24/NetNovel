package com.example.netnovel_server.chatbot.service.embedding;

import com.example.netnovel_server.chatbot.config.ChatbotEmbeddingProperties;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.model.ChatbotSemanticMatch;
import com.example.netnovel_server.chatbot.repository.ChatbotEmbeddingDocumentRepository;
import com.example.netnovel_server.chatbot.service.ChatbotKnowledgeBase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatbotSemanticMatcher {

    private final ChatbotEmbeddingProperties properties;
    private final ChatbotEmbeddingClient embeddingClient;
    private final ChatbotEmbeddingDocumentRepository repository;
    private final ChatbotKnowledgeBase knowledgeBase;

    public ChatbotSemanticMatcher(
        ChatbotEmbeddingProperties properties,
        ChatbotEmbeddingClient embeddingClient,
        ChatbotEmbeddingDocumentRepository repository,
        ChatbotKnowledgeBase knowledgeBase
    ) {
        this.properties = properties;
        this.embeddingClient = embeddingClient;
        this.repository = repository;
        this.knowledgeBase = knowledgeBase;
    }

    public Optional<ChatbotMatchResult> match(String message, ChatbotLanguage language) {
        if (!properties.enabled() || message == null || message.isBlank()) {
            return Optional.empty();
        }

        try {
            List<Double> embedding = embeddingClient.embedQuery(message);
            if (embedding.isEmpty()) {
                return Optional.empty();
            }

            return repository.findNearest(language.code(), properties.model(), embedding, properties.topK()).stream()
                .filter(match -> match.similarity() >= properties.minSimilarity())
                .findFirst()
                .flatMap(match -> toChatbotMatch(match, language));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private Optional<ChatbotMatchResult> toChatbotMatch(ChatbotSemanticMatch semanticMatch, ChatbotLanguage language) {
        if ("faq".equals(semanticMatch.documentType())) {
            Optional<ChatbotFaq> faq = knowledgeBase.findFaqById(semanticMatch.sourceId());
            return faq.map(value -> new ChatbotMatchResult(
                "faq",
                language,
                semanticMatch.similarity(),
                0.0,
                false,
                null,
                Map.of(),
                value,
                null
            ));
        }

        if ("intent".equals(semanticMatch.documentType())) {
            Optional<ChatbotIntent> intent = knowledgeBase.findIntentById(semanticMatch.sourceId());
            return intent.map(value -> new ChatbotMatchResult(
                value.id(),
                language,
                semanticMatch.similarity(),
                0.0,
                false,
                null,
                value.filters() == null ? Map.of() : value.filters(),
                null,
                value
            ));
        }

        return Optional.empty();
    }
}

