package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.UserEventDataReportDTO;
import com.example.netnovel_server.service.RecommendationAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-reports")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
@Tag(name = "Data Reports", description = "Admin data-quality reports for operational and recommendation analysis")
public class DataReportController {

    private final RecommendationAnalyticsService recommendationAnalyticsService;

    public DataReportController(RecommendationAnalyticsService recommendationAnalyticsService) {
        this.recommendationAnalyticsService = recommendationAnalyticsService;
    }

    @GetMapping("/user-events")
    @Operation(summary = "Get user-event data readiness metrics")
    public ResponseEntity<UserEventDataReportDTO> getUserEventReport(
        @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(recommendationAnalyticsService.getUserEventReport(days));
    }
}
