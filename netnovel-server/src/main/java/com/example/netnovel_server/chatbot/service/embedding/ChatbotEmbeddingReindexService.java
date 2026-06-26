package com.example.netnovel_server.chatbot.service.embedding;

import com.example.netnovel_server.chatbot.config.ChatbotEmbeddingProperties;
import com.example.netnovel_server.chatbot.dto.ChatbotEmbeddingReindexResponseDTO;
import com.example.netnovel_server.chatbot.model.ChatbotEmbeddingDocumentCandidate;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.repository.ChatbotEmbeddingDocumentRepository;
import com.example.netnovel_server.chatbot.service.ChatbotKnowledgeBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotEmbeddingReindexService {

    private final ChatbotEmbeddingProperties properties;
    private final ChatbotKnowledgeBase knowledgeBase;
    private final ChatbotEmbeddingClient embeddingClient;
    private final ChatbotEmbeddingDocumentRepository repository;

    public ChatbotEmbeddingReindexService(
        ChatbotEmbeddingProperties properties,
        ChatbotKnowledgeBase knowledgeBase,
        ChatbotEmbeddingClient embeddingClient,
        ChatbotEmbeddingDocumentRepository repository
    ) {
        this.properties = properties;
        this.knowledgeBase = knowledgeBase;
        this.embeddingClient = embeddingClient;
        this.repository = repository;
    }

    @Transactional
    public ChatbotEmbeddingReindexResponseDTO reindex() {
        if (!properties.enabled()) {
            return ChatbotEmbeddingReindexResponseDTO.builder()
                .enabled(false)
                .model(properties.model())
                .dimension(properties.dimension())
                .documents(0)
                .batches(0)
                .message("Chatbot embedding is disabled. Set CHATBOT_EMBEDDING_ENABLED=true to enable it.")
                .build();
        }

        repository.initializeSchema();
        List<ChatbotEmbeddingDocumentCandidate> candidates = candidates();
        repository.deactivateAllForModel(properties.model());

        int batches = 0;
        for (int start = 0; start < candidates.size(); start += properties.reindexBatchSize()) {
            int end = Math.min(start + properties.reindexBatchSize(), candidates.size());
            List<ChatbotEmbeddingDocumentCandidate> batch = candidates.subList(start, end);
            List<List<Double>> embeddings = embeddingClient.embedPassages(batch.stream()
                .map(ChatbotEmbeddingDocumentCandidate::content)
                .toList());

            if (embeddings.size() != batch.size()) {
                throw new IllegalStateException("Embedding service returned " + embeddings.size()
                    + " embeddings for " + batch.size() + " documents");
            }

            for (int index = 0; index < batch.size(); index++) {
                repository.upsert(batch.get(index), embeddings.get(index), properties.model());
            }
            batches++;
        }

        return ChatbotEmbeddingReindexResponseDTO.builder()
            .enabled(true)
            .model(properties.model())
            .dimension(properties.dimension())
            .documents(candidates.size())
            .batches(batches)
            .message("Chatbot embedding documents reindexed successfully.")
            .build();
    }

    private List<ChatbotEmbeddingDocumentCandidate> candidates() {
        List<ChatbotEmbeddingDocumentCandidate> documents = new ArrayList<>();
        for (ChatbotFaq faq : knowledgeBase.faqs()) {
            for (Map.Entry<String, List<String>> entry : faq.examples().entrySet()) {
                for (String example : entry.getValue()) {
                    documents.add(candidate(
                        "faq",
                        faq.id(),
                        entry.getKey(),
                        example,
                        metadata(
                            "type", faq.type(),
                            "tags", faq.tags() == null ? List.of() : faq.tags(),
                            "actionUrls", faq.actionUrls() == null ? List.of() : faq.actionUrls()
                        )
                    ));
                }
            }
        }

        for (ChatbotIntent intent : knowledgeBase.intents()) {
            for (Map.Entry<String, List<String>> entry : intent.examples().entrySet()) {
                for (String example : entry.getValue()) {
                    documents.add(candidate(
                        "intent",
                        intent.id(),
                        entry.getKey(),
                        example,
                        metadata(
                            "type", intent.type(),
                            "filters", intent.filters() == null ? Map.of() : intent.filters(),
                            "tags", intent.tags() == null ? List.of() : intent.tags()
                        )
                    ));
                }
            }
        }

        return documents;
    }

    private ChatbotEmbeddingDocumentCandidate candidate(
        String documentType,
        String sourceId,
        String language,
        String content,
        Map<String, Object> metadata
    ) {
        return new ChatbotEmbeddingDocumentCandidate(
            documentType,
            sourceId,
            language,
            content,
            sha256(String.join("|", properties.model(), documentType, sourceId, language, content)),
            metadata
        );
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private Map<String, Object> metadata(Object... keysAndValues) {
        Map<String, Object> metadata = new HashMap<>();
        for (int index = 0; index < keysAndValues.length; index += 2) {
            Object value = keysAndValues[index + 1];
            if (value != null) {
                metadata.put((String) keysAndValues[index], value);
            }
        }
        return metadata;
    }
}

