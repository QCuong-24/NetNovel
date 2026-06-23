package com.example.netnovel_server.recommendation.dto;

import java.time.LocalDateTime;

public record UserNovelInteractionDTO(
    Long userId,
    Long novelId,
    Long viewNovelCount,
    Long viewChapterCount,
    Long commentCount,
    Long replyCount,
    boolean followed,
    boolean liked,
    boolean bookmarked,
    double interactionScore,
    LocalDateTime firstInteractedAt,
    LocalDateTime lastInteractedAt,
    LocalDateTime calculatedAt
) {
}
