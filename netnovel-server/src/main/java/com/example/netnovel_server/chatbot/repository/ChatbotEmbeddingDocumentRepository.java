package com.example.netnovel_server.chatbot.repository;

import com.example.netnovel_server.chatbot.config.ChatbotEmbeddingProperties;
import com.example.netnovel_server.chatbot.dto.ChatbotEmbeddingStatusDTO;
import com.example.netnovel_server.chatbot.model.ChatbotEmbeddingDocumentCandidate;
import com.example.netnovel_server.chatbot.model.ChatbotSemanticMatch;
import com.example.netnovel_server.chatbot.service.embedding.ChatbotEmbeddingVectorFormatter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ChatbotEmbeddingDocumentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ChatbotEmbeddingProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatbotEmbeddingDocumentRepository(JdbcTemplate jdbcTemplate, ChatbotEmbeddingProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    public void initializeSchema() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS chatbot_embedding_documents (
                id BIGSERIAL PRIMARY KEY,
                document_type VARCHAR(32) NOT NULL,
                source_id VARCHAR(128) NOT NULL,
                language VARCHAR(8) NOT NULL,
                content TEXT NOT NULL,
                content_hash VARCHAR(64) NOT NULL,
                model_name VARCHAR(255) NOT NULL,
                embedding vector(%d) NOT NULL,
                metadata JSONB,
                active BOOLEAN NOT NULL DEFAULT TRUE,
                created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                updated_at TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """.formatted(properties.dimension()));
        jdbcTemplate.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS uk_chatbot_embedding_documents_hash
            ON chatbot_embedding_documents(model_name, document_type, source_id, language, content_hash)
            """);
        jdbcTemplate.execute("""
            CREATE INDEX IF NOT EXISTS idx_chatbot_embedding_documents_lookup
            ON chatbot_embedding_documents(model_name, active, language, document_type)
            """);
        jdbcTemplate.execute("""
            CREATE INDEX IF NOT EXISTS idx_chatbot_embedding_documents_embedding
            ON chatbot_embedding_documents USING hnsw (embedding vector_cosine_ops)
            """);
    }

    public void deactivateAllForModel(String modelName) {
        jdbcTemplate.update("""
            UPDATE chatbot_embedding_documents
            SET active = FALSE, updated_at = NOW()
            WHERE model_name = ?
            """, modelName);
    }

    public void upsert(ChatbotEmbeddingDocumentCandidate candidate, List<Double> embedding, String modelName) {
        jdbcTemplate.update("""
            INSERT INTO chatbot_embedding_documents (
                document_type, source_id, language, content, content_hash, model_name, embedding, metadata, active
            )
            VALUES (?, ?, ?, ?, ?, ?, ?::vector, ?::jsonb, TRUE)
            ON CONFLICT (model_name, document_type, source_id, language, content_hash)
            DO UPDATE SET
                content = EXCLUDED.content,
                embedding = EXCLUDED.embedding,
                metadata = EXCLUDED.metadata,
                active = TRUE,
                updated_at = NOW()
            """,
            candidate.documentType(),
            candidate.sourceId(),
            candidate.language(),
            candidate.content(),
            candidate.contentHash(),
            modelName,
            ChatbotEmbeddingVectorFormatter.toPgVector(embedding),
            metadataJson(candidate));
    }

    public List<ChatbotSemanticMatch> findNearest(String language, String modelName, List<Double> embedding, int limit) {
        String vector = ChatbotEmbeddingVectorFormatter.toPgVector(embedding);
        return jdbcTemplate.query("""
            SELECT id, document_type, source_id, language, content, metadata::text AS metadata_json,
                   1 - (embedding <=> ?::vector) AS similarity
            FROM chatbot_embedding_documents
            WHERE active = TRUE
              AND model_name = ?
              AND language = ?
            ORDER BY embedding <=> ?::vector
            LIMIT ?
            """,
            this::mapSemanticMatch,
            vector,
            modelName,
            language,
            vector,
            limit);
    }

    public boolean tableExists() {
        Boolean exists = jdbcTemplate.queryForObject(
            "SELECT to_regclass('public.chatbot_embedding_documents') IS NOT NULL",
            Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    public ChatbotEmbeddingStatusDTO status(String modelName) {
        return jdbcTemplate.queryForObject("""
            SELECT
                COUNT(*) AS total_documents,
                COUNT(*) FILTER (WHERE active = TRUE) AS active_documents,
                COUNT(*) FILTER (WHERE active = TRUE AND document_type = 'faq') AS faq_documents,
                COUNT(*) FILTER (WHERE active = TRUE AND document_type = 'intent') AS intent_documents,
                MAX(updated_at) FILTER (WHERE active = TRUE) AS last_indexed_at
            FROM chatbot_embedding_documents
            WHERE model_name = ?
            """,
            (resultSet, rowNum) -> ChatbotEmbeddingStatusDTO.builder()
                .model(modelName)
                .totalDocuments(resultSet.getLong("total_documents"))
                .activeDocuments(resultSet.getLong("active_documents"))
                .faqDocuments(resultSet.getLong("faq_documents"))
                .intentDocuments(resultSet.getLong("intent_documents"))
                .lastIndexedAt(toLocalDateTime(resultSet.getTimestamp("last_indexed_at")))
                .build(),
            modelName
        );
    }

    private ChatbotSemanticMatch mapSemanticMatch(ResultSet resultSet, int rowNum) throws SQLException {
        return new ChatbotSemanticMatch(
            resultSet.getLong("id"),
            resultSet.getString("document_type"),
            resultSet.getString("source_id"),
            resultSet.getString("language"),
            resultSet.getString("content"),
            resultSet.getString("metadata_json"),
            resultSet.getDouble("similarity")
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String metadataJson(ChatbotEmbeddingDocumentCandidate candidate) {
        try {
            return objectMapper.writeValueAsString(candidate.metadata());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize chatbot embedding metadata", exception);
        }
    }
}

