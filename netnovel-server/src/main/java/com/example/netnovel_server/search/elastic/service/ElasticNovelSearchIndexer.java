package com.example.netnovel_server.search.elastic.service;

import com.example.netnovel_server.dto.ElasticReindexResponseDTO;
import com.example.netnovel_server.embedding.config.NovelEmbeddingProperties;
import com.example.netnovel_server.embedding.service.NovelEmbeddingClient;
import com.example.netnovel_server.embedding.service.NovelEmbeddingTextBuilder;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.exception.SearchUnavailableException;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.search.elastic.document.ElasticNovelDocument;
import com.example.netnovel_server.search.elastic.mapper.NovelSearchDocumentMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "app.search.elastic", name = "enabled", havingValue = "true")
public class ElasticNovelSearchIndexer {

    private static final Logger log = LoggerFactory.getLogger(ElasticNovelSearchIndexer.class);

    private final RestClient restClient;
    private final NovelRepository novelRepository;
    private final NovelSearchDocumentMapper documentMapper;
    private final ElasticNovelIndexManager indexManager;
    private final NovelEmbeddingProperties embeddingProperties;
    private final NovelEmbeddingClient embeddingClient;
    private final NovelEmbeddingTextBuilder embeddingTextBuilder;
    private final ObjectMapper objectMapper;

    public ElasticNovelSearchIndexer(
        RestClient restClient,
        NovelRepository novelRepository,
        NovelSearchDocumentMapper documentMapper,
        ElasticNovelIndexManager indexManager,
        NovelEmbeddingProperties embeddingProperties,
        NovelEmbeddingClient embeddingClient,
        NovelEmbeddingTextBuilder embeddingTextBuilder
    ) {
        this.restClient = restClient;
        this.novelRepository = novelRepository;
        this.documentMapper = documentMapper;
        this.indexManager = indexManager;
        this.embeddingProperties = embeddingProperties;
        this.embeddingClient = embeddingClient;
        this.embeddingTextBuilder = embeddingTextBuilder;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional(readOnly = true)
    public ElasticReindexResponseDTO reindexAllNovels() {
        indexManager.ensureNovelIndex();
        return indexAllNovels();
    }

    @Transactional(readOnly = true)
    public ElasticReindexResponseDTO rebuildNovelIndex() {
        indexManager.deleteNovelIndexIfExists();
        indexManager.ensureNovelIndex();
        return indexAllNovels();
    }

    private ElasticReindexResponseDTO indexAllNovels() {
        int indexed = 0;
        int failed = 0;
        List<Novel> novels = novelRepository.findAll();
        int batchSize = embeddingProperties.reindexBatchSize();
        for (int start = 0; start < novels.size(); start += batchSize) {
            List<Novel> batch = novels.subList(start, Math.min(start + batchSize, novels.size()));
            List<ElasticNovelDocument> documents = buildDocuments(batch);
            for (ElasticNovelDocument document : documents) {
                try {
                    indexDocument(document);
                    indexed++;
                } catch (Exception exception) {
                    failed++;
                    log.warn("Could not index novel. novelId={}", document.getNovelId(), exception);
                }
            }
        }

        return ElasticReindexResponseDTO.builder()
            .indexName(indexManager.getNovelIndexName())
            .indexed(indexed)
            .failed(failed)
            .build();
    }

    @Transactional(readOnly = true)
    public void indexNovel(Long novelId) {
        Novel novel = novelRepository.findById(novelId)
            .orElseThrow(() -> new IllegalArgumentException("Novel not found: " + novelId));
        indexNovel(novel);
    }

    public void deleteNovel(Long novelId) {
        Request request = new Request("DELETE", "/" + indexManager.getNovelIndexName() + "/_doc/" + novelId);
        try {
            restClient.performRequest(request);
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not delete Elasticsearch novel document: " + novelId, exception);
        }
    }

    private void indexNovel(Novel novel) {
        ElasticNovelDocument document = documentMapper.toDocument(novel);
        enrichWithEmbedding(List.of(novel), List.of(document));
        indexDocument(document);
    }

    private List<ElasticNovelDocument> buildDocuments(List<Novel> novels) {
        List<ElasticNovelDocument> documents = new ArrayList<>(novels.size());
        for (Novel novel : novels) {
            documents.add(documentMapper.toDocument(novel));
        }
        enrichWithEmbedding(novels, documents);
        return documents;
    }

    private void enrichWithEmbedding(List<Novel> novels, List<ElasticNovelDocument> documents) {
        if (!embeddingProperties.enabled() || documents.isEmpty()) {
            return;
        }

        List<String> embeddingTexts = novels.stream()
            .map(embeddingTextBuilder::build)
            .toList();
        for (int index = 0; index < documents.size(); index++) {
            documents.get(index).setEmbeddingText(embeddingTexts.get(index));
        }

        try {
            List<List<Double>> vectors = embeddingClient.embedPassages(embeddingTexts);
            LocalDateTime now = LocalDateTime.now();
            for (int index = 0; index < documents.size() && index < vectors.size(); index++) {
                List<Double> vector = vectors.get(index);
                if (vector == null || vector.isEmpty()) {
                    continue;
                }
                ElasticNovelDocument document = documents.get(index);
                document.setEmbeddingVector(vector);
                document.setEmbeddingModel(embeddingProperties.model());
                document.setEmbeddingDimension(embeddingProperties.dimension());
                document.setEmbeddingUpdatedAt(now);
            }
        } catch (Exception exception) {
            if (embeddingProperties.failOnIndexError()) {
                throw new IllegalStateException("Could not enrich Elasticsearch novel documents with embeddings", exception);
            }
            log.warn("Could not enrich Elasticsearch novel documents with embeddings. indexedKeywordOnly={}", documents.size(), exception);
        }
    }

    private void indexDocument(ElasticNovelDocument document) {
        Request request = new Request("PUT", "/" + indexManager.getNovelIndexName() + "/_doc/" + document.getNovelId());
        request.setJsonEntity(toJson(document));
        try {
            restClient.performRequest(request);
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not index Elasticsearch novel document: " + document.getNovelId(), exception);
        }
    }

    private String toJson(ElasticNovelDocument document) {
        try {
            return objectMapper.writeValueAsString(toMap(document));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize novel search document", exception);
        }
    }

    private Map<String, Object> toMap(ElasticNovelDocument document) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("novelId", document.getNovelId());
        values.put("title", document.getTitle());
        values.put("author", document.getAuthor());
        values.put("description", document.getDescription());
        values.put("genres", document.getGenres());
        values.put("tags", document.getTags());
        values.put("status", document.getStatus());
        values.put("views", document.getViews());
        values.put("follows", document.getFollows());
        values.put("likes", document.getLikes());
        values.put("bookmarks", document.getBookmarks());
        values.put("chapterCount", document.getChapterCount());
        values.put("latestChapterNumber", document.getLatestChapterNumber());
        values.put("lastChapterUpdatedAt", formatDate(document.getLastChapterUpdatedAt()));
        values.put("createdAt", formatDate(document.getCreatedAt()));
        values.put("updatedAt", formatDate(document.getUpdatedAt()));
        values.put("crawled", document.getCrawled());
        values.put("sourceName", document.getSourceName());
        values.put("sourceNovelUrl", document.getSourceNovelUrl());
        values.put("popularityScore", document.getPopularityScore());
        values.put("freshnessScore", document.getFreshnessScore());
        values.put("recommendationText", document.getRecommendationText());
        values.put("embeddingText", document.getEmbeddingText());
        values.put("embeddingVector", document.getEmbeddingVector());
        values.put("embeddingModel", document.getEmbeddingModel());
        values.put("embeddingDimension", document.getEmbeddingDimension());
        values.put("embeddingUpdatedAt", formatDate(document.getEmbeddingUpdatedAt()));
        return values;
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? null : value.toString();
    }
}
