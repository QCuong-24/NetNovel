package com.example.netnovel_server.recommendation.dto;

import java.time.LocalDateTime;

public record UserNovelInteractionRebuildDTO(
    int interactionCount,
    LocalDateTime rebuiltAt
) {
}
