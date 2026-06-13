package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.dto.SearchSuggestionDTO;
import com.example.netnovel_server.service.PostgresNovelSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "PostgreSQL-backed public search APIs")
public class SearchController {

    private final PostgresNovelSearchService searchService;

    public SearchController(PostgresNovelSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/novels")
    @Operation(summary = "Search novels using PostgreSQL")
    public ResponseEntity<Page<NovelSearchResultDTO>> searchNovels(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String tag,
        @RequestParam(defaultValue = "relevance") String sortMode,
        Pageable pageable
    ) {
        return ResponseEntity.ok(searchService.searchNovels(q, status, tag, sortMode, pageable));
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get search suggestions using PostgreSQL")
    public ResponseEntity<List<SearchSuggestionDTO>> suggest(
        @RequestParam String q,
        @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(searchService.suggest(q, limit));
    }
}
