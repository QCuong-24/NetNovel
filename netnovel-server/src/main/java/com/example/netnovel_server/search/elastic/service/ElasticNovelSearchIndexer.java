package com.example.netnovel_server.search.elastic.service;

import com.example.netnovel_server.dto.ElasticReindexResponseDTO;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.exception.SearchUnavailableException;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.search.elastic.document.NovelSearchDocument;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "app.search.elastic", name = "enabled", havingValue = "true")
public class ElasticNovelSearchIndexer {

    private static final Logger log = LoggerFactory.getLogger(ElasticNovelSearchIndexer.class);

    private final RestClient restClient;
    private final NovelRepository novelRepository;
    private final NovelSearchDocumentMapper documentMapper;
    private final ElasticNovelIndexManager indexManager;
    private final ObjectMapper objectMapper;

    public ElasticNovelSearchIndexer(
        RestClient restClient,
        NovelRepository novelRepository,
        NovelSearchDocumentMapper documentMapper,
        ElasticNovelIndexManager indexManager
    ) {
        this.restClient = restClient;
        this.novelRepository = novelRepository;
        this.documentMapper = documentMapper;
        this.indexManager = indexManager;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional(readOnly = true)
    public ElasticReindexResponseDTO reindexAllNovels() {
        indexManager.ensureNovelIndex();

        int indexed = 0;
        int failed = 0;
        for (Novel novel : novelRepository.findAll()) {
            try {
                indexNovel(novel);
                indexed++;
            } catch (Exception exception) {
                failed++;
                log.warn("Could not index novel. novelId={}", novel.getId(), exception);
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
        NovelSearchDocument document = documentMapper.toDocument(novel);
        Request request = new Request("PUT", "/" + indexManager.getNovelIndexName() + "/_doc/" + novel.getId());
        request.setJsonEntity(toJson(document));
        try {
            restClient.performRequest(request);
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not index Elasticsearch novel document: " + novel.getId(), exception);
        }
    }

    private String toJson(NovelSearchDocument document) {
        try {
            return objectMapper.writeValueAsString(toMap(document));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize novel search document", exception);
        }
    }

    private Map<String, Object> toMap(NovelSearchDocument document) {
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
        return values;
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? null : value.toString();
    }
}
