package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.ElasticReindexResponseDTO;
import com.example.netnovel_server.dto.ElasticDiagnosticsResponseDTO;
import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.entity.UserEventType;
import com.example.netnovel_server.search.elastic.service.ElasticDiagnosticsService;
import com.example.netnovel_server.search.elastic.service.ElasticAdminNovelSearchService;
import com.example.netnovel_server.search.elastic.service.ElasticNovelSearchIndexer;
import com.example.netnovel_server.service.UserEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/advanced/search")
@Tag(name = "Advanced Search", description = "Elasticsearch-backed advanced search APIs")
public class AdvancedSearchController {

    private final ObjectProvider<ElasticNovelSearchIndexer> indexerProvider;
    private final ObjectProvider<ElasticAdminNovelSearchService> searchServiceProvider;
    private final ObjectProvider<ElasticDiagnosticsService> diagnosticsServiceProvider;
    private final UserEventService userEventService;

    public AdvancedSearchController(
        ObjectProvider<ElasticNovelSearchIndexer> indexerProvider,
        ObjectProvider<ElasticAdminNovelSearchService> searchServiceProvider,
        ObjectProvider<ElasticDiagnosticsService> diagnosticsServiceProvider,
        UserEventService userEventService
    ) {
        this.indexerProvider = indexerProvider;
        this.searchServiceProvider = searchServiceProvider;
        this.diagnosticsServiceProvider = diagnosticsServiceProvider;
        this.userEventService = userEventService;
    }

    @PostMapping("/reindex/novels")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reindex all novels into Elasticsearch")
    public ResponseEntity<ElasticReindexResponseDTO> reindexNovels() {
        return ResponseEntity.ok(elasticIndexer().reindexAllNovels());
    }

    @PostMapping("/reindex/novels/rebuild")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete, recreate, and reindex the Elasticsearch novel index")
    public ResponseEntity<ElasticReindexResponseDTO> rebuildNovelIndex() {
        return ResponseEntity.ok(elasticIndexer().rebuildNovelIndex());
    }

    @GetMapping("/diagnostics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Elasticsearch novel index diagnostics")
    public ResponseEntity<ElasticDiagnosticsResponseDTO> diagnostics() {
        return ResponseEntity.ok(elasticDiagnosticsService().diagnostics());
    }

    @GetMapping("/novels")
    @Operation(summary = "Search novels using Elasticsearch")
    public ResponseEntity<Page<NovelSearchResultDTO>> searchNovels(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) List<String> genre,
        @RequestParam(required = false) List<String> tag,
        @RequestParam(required = false) String source,
        @RequestParam(required = false) Boolean crawled,
        Pageable pageable
    ) {
        if (q != null && !q.isBlank()) {
            userEventService.recordForCurrentUser(UserEventType.SEARCH);
        }
        return ResponseEntity.ok(elasticSearchService().searchNovels(q, status, genre, tag, source, crawled, pageable));
    }

    private ElasticNovelSearchIndexer elasticIndexer() {
        ElasticNovelSearchIndexer indexer = indexerProvider.getIfAvailable();
        if (indexer == null) {
            throw searchDisabledException();
        }
        return indexer;
    }

    private ElasticAdminNovelSearchService elasticSearchService() {
        ElasticAdminNovelSearchService service = searchServiceProvider.getIfAvailable();
        if (service == null) {
            throw searchDisabledException();
        }
        return service;
    }

    private ElasticDiagnosticsService elasticDiagnosticsService() {
        ElasticDiagnosticsService service = diagnosticsServiceProvider.getIfAvailable();
        if (service == null) {
            throw searchDisabledException();
        }
        return service;
    }

    private ResponseStatusException searchDisabledException() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Elasticsearch search is disabled");
    }
}
