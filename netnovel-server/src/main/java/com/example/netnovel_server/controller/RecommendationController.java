package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.recommendation.dto.RecommendationItemDTO;
import com.example.netnovel_server.recommendation.dto.SimilarNovelRecommendationDTO;
import com.example.netnovel_server.recommendation.service.ElasticSemanticRecommendationService;
import com.example.netnovel_server.recommendation.service.HybridRecommendationService;
import com.example.netnovel_server.recommendation.service.HybridSimilarNovelRecommendationService;
import com.example.netnovel_server.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "Novel recommendation APIs")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final ObjectProvider<ElasticSemanticRecommendationService> semanticRecommendationServiceProvider;
    private final HybridSimilarNovelRecommendationService hybridSimilarNovelRecommendationService;
    private final HybridRecommendationService hybridRecommendationService;

    public RecommendationController(
        RecommendationService recommendationService,
        ObjectProvider<ElasticSemanticRecommendationService> semanticRecommendationServiceProvider,
        HybridSimilarNovelRecommendationService hybridSimilarNovelRecommendationService,
        HybridRecommendationService hybridRecommendationService
    ) {
        this.recommendationService = recommendationService;
        this.semanticRecommendationServiceProvider = semanticRecommendationServiceProvider;
        this.hybridSimilarNovelRecommendationService = hybridSimilarNovelRecommendationService;
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

    @GetMapping("/novels/{novelId}/similar/semantic")
    @Operation(summary = "Get semantic similar novels using Elasticsearch embeddings")
    public ResponseEntity<Page<NovelSearchResultDTO>> getSemanticSimilarNovels(
        @PathVariable Long novelId,
        Pageable pageable
    ) {
        ElasticSemanticRecommendationService service = semanticRecommendationServiceProvider.getIfAvailable();
        if (service == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Semantic recommendations are disabled");
        }
        return ResponseEntity.ok(service.getSimilarNovels(novelId, pageable));
    }

    @GetMapping("/novels/{novelId}/similar/hybrid")
    @Operation(summary = "Get explainable hybrid similar novels")
    public ResponseEntity<Page<SimilarNovelRecommendationDTO>> getHybridSimilarNovels(
        @PathVariable Long novelId,
        Pageable pageable
    ) {
        return ResponseEntity.ok(hybridSimilarNovelRecommendationService.getSimilarNovels(novelId, pageable));
    }

    @GetMapping("/for-you")
    @Operation(summary = "Get hybrid personalized novel recommendations")
    public ResponseEntity<List<RecommendationItemDTO>> getForYou(
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(hybridRecommendationService.getForCurrentUser(size));
    }
}
