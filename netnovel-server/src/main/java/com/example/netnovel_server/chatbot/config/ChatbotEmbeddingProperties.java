package com.example.netnovel_server.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChatbotEmbeddingProperties {

    private final boolean enabled;
    private final String baseUrl;
    private final String model;
    private final int dimension;
    private final int topK;
    private final double minSimilarity;
    private final int reindexBatchSize;

    public ChatbotEmbeddingProperties(
        @Value("${app.chatbot.embedding.enabled:false}") boolean enabled,
        @Value("${app.chatbot.embedding.base-url:http://embedding:8000}") String baseUrl,
        @Value("${app.chatbot.embedding.model:intfloat/multilingual-e5-small}") String model,
        @Value("${app.chatbot.embedding.dimension:384}") int dimension,
        @Value("${app.chatbot.embedding.top-k:5}") int topK,
        @Value("${app.chatbot.embedding.min-similarity:0.82}") double minSimilarity,
        @Value("${app.chatbot.embedding.reindex-batch-size:16}") int reindexBatchSize
    ) {
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.model = model;
        this.dimension = dimension;
        this.topK = topK;
        this.minSimilarity = minSimilarity;
        this.reindexBatchSize = reindexBatchSize;
    }

    public boolean enabled() {
        return enabled;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String model() {
        return model;
    }

    public int dimension() {
        return dimension;
    }

    public int topK() {
        return topK;
    }

    public double minSimilarity() {
        return minSimilarity;
    }

    public int reindexBatchSize() {
        return reindexBatchSize;
    }
}
