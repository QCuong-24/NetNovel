package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.dto.SearchSuggestionDTO;
import com.example.netnovel_server.search.postgresql.PostgresNovelSearchService;
import com.example.netnovel_server.entity.UserEventType;
import com.example.netnovel_server.service.UserEventService;
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
    private final UserEventService userEventService;

    public SearchController(PostgresNovelSearchService searchService, UserEventService userEventService) {
        this.searchService = searchService;
        this.userEventService = userEventService;
    }

    @GetMapping("/novels")
    @Operation(summary = "Search novels using PostgreSQL")
    public ResponseEntity<Page<NovelSearchResultDTO>> searchNovels(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) List<String> genre,
        @RequestParam(defaultValue = "relevance") String sortMode,
        Pageable pageable
    ) {
        if (q != null && !q.isBlank()) {
            userEventService.recordForCurrentUser(UserEventType.SEARCH);
        }
        return ResponseEntity.ok(searchService.searchNovels(q, status, genre, sortMode, pageable));
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
