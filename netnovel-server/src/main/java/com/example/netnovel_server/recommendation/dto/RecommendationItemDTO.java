package com.example.netnovel_server.recommendation.dto;

import com.example.netnovel_server.dto.NovelDTO;

public record RecommendationItemDTO(
    NovelDTO novel,
    double score,
    String reason
) {
}
