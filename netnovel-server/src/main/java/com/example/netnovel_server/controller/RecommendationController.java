package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.recommendation.dto.RecommendationItemDTO;
import com.example.netnovel_server.recommendation.service.HybridRecommendationService;
import com.example.netnovel_server.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "Novel recommendation APIs")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final HybridRecommendationService hybridRecommendationService;

    public RecommendationController(
        RecommendationService recommendationService,
        HybridRecommendationService hybridRecommendationService
    ) {
        this.recommendationService = recommendationService;
        this.hybridRecommendationService = hybridRecommendationService;
    }

    @GetMapping("/novels/{novelId}/similar")
    @Operation(summary = "Get content-based similar novels")
    public ResponseEntity<Page<NovelSearchResultDTO>> getSimilarNovels(
        @PathVariable Long novelId,
        Pageable pageable
    ) {
        return ResponseEntity.ok(recommendationService.getSimilarNovels(novelId, pageable));
    }

    @GetMapping("/for-you")
    @Operation(summary = "Get hybrid personalized novel recommendations")
    public ResponseEntity<List<RecommendationItemDTO>> getForYou(
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(hybridRecommendationService.getForCurrentUser(size));
    }
}
