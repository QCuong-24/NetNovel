package com.example.netnovel_server.embedding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NovelEmbeddingProperties {

    private final boolean enabled;
    private final String baseUrl;
    private final String model;
    private final int dimension;
    private final int reindexBatchSize;
    private final int topK;
    private final double minSimilarity;
    private final boolean failOnIndexError;

    public NovelEmbeddingProperties(
        @Value("${app.novel.embedding.enabled:false}") boolean enabled,
        @Value("${app.novel.embedding.base-url:${EMBEDDING_BASE_URL:http://localhost:8000}}") String baseUrl,
        @Value("${app.novel.embedding.model:${EMBEDDING_MODEL:intfloat/multilingual-e5-small}}") String model,
        @Value("${app.novel.embedding.dimension:384}") int dimension,
        @Value("${app.novel.embedding.reindex-batch-size:16}") int reindexBatchSize,
        @Value("${app.novel.embedding.top-k:10}") int topK,
        @Value("${app.novel.embedding.min-similarity:0.0}") double minSimilarity,
        @Value("${app.novel.embedding.fail-on-index-error:false}") boolean failOnIndexError
    ) {
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.model = model;
        this.dimension = dimension;
        this.reindexBatchSize = Math.max(1, reindexBatchSize);
        this.topK = Math.max(1, topK);
        this.minSimilarity = minSimilarity;
        this.failOnIndexError = failOnIndexError;
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

    public int reindexBatchSize() {
        return reindexBatchSize;
    }

    public int topK() {
        return topK;
    }

    public double minSimilarity() {
        return minSimilarity;
    }

    public boolean failOnIndexError() {
        return failOnIndexError;
    }
}
