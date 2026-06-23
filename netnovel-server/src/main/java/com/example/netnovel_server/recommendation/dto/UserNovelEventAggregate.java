package com.example.netnovel_server.recommendation.dto;

import java.time.LocalDateTime;

public record UserNovelEventAggregate(
    Long userId,
    Long novelId,
    Long viewNovelCount,
    Long viewChapterCount,
    Long commentCount,
    Long replyCount,
    LocalDateTime firstInteractedAt,
    LocalDateTime lastInteractedAt
) {
}
