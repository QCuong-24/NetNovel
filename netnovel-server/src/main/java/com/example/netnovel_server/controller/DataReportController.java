package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.UserEventDataReportDTO;
import com.example.netnovel_server.recommendation.dto.UserNovelInteractionRebuildDTO;
import com.example.netnovel_server.recommendation.dto.UserNovelInteractionDTO;
import com.example.netnovel_server.recommendation.service.UserNovelInteractionAggregationService;
import com.example.netnovel_server.service.RecommendationAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-reports")
@Tag(name = "Data Reports", description = "Admin data-quality reports for operational and recommendation analysis")
public class DataReportController {

    private final RecommendationAnalyticsService recommendationAnalyticsService;
    private final UserNovelInteractionAggregationService userNovelInteractionAggregationService;

    public DataReportController(
        RecommendationAnalyticsService recommendationAnalyticsService,
        UserNovelInteractionAggregationService userNovelInteractionAggregationService
    ) {
        this.recommendationAnalyticsService = recommendationAnalyticsService;
        this.userNovelInteractionAggregationService = userNovelInteractionAggregationService;
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/user-events")
    @Operation(summary = "Get user-event data readiness metrics")
    public ResponseEntity<UserEventDataReportDTO> getUserEventReport(
        @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(recommendationAnalyticsService.getUserEventReport(days));
    }

    @PostMapping("/user-novel-interactions/rebuild")
    @Operation(summary = "Rebuild aggregated user-novel interactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserNovelInteractionRebuildDTO> rebuildUserNovelInteractions() {
        return ResponseEntity.ok(userNovelInteractionAggregationService.rebuild());
    }

    @GetMapping("/user-novel-interactions")
    @Operation(summary = "Inspect aggregated user-novel interactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserNovelInteractionDTO>> getUserNovelInteractions(
        @RequestParam(required = false) Long userId,
        Pageable pageable
    ) {
        return ResponseEntity.ok(userNovelInteractionAggregationService.getInteractions(userId, pageable));
    }
}
