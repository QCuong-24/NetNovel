package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "Novel recommendation APIs")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/novels/{novelId}/similar")
    @Operation(summary = "Get content-based similar novels")
    public ResponseEntity<Page<NovelSearchResultDTO>> getSimilarNovels(
        @PathVariable Long novelId,
        Pageable pageable
    ) {
        return ResponseEntity.ok(recommendationService.getSimilarNovels(novelId, pageable));
    }
}
