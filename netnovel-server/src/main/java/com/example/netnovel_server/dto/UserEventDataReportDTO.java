package com.example.netnovel_server.dto;

import com.example.netnovel_server.entity.UserEventType;

import java.time.LocalDateTime;
import java.util.Map;

public record UserEventDataReportDTO(
    int periodDays,
    LocalDateTime from,
    LocalDateTime to,
    long totalEvents,
    Map<UserEventType, Long> eventsByType,
    long activeUsers,
    long interactedNovels,
    long usersWithAtLeast3DistinctNovels,
    long usersWithAtLeast5DistinctNovels,
    long novelsWithAtLeast3Users,
    long novelsWithAtLeast5Users,
    double averageDistinctNovelsPerActiveUser,
    double averageUsersPerInteractedNovel
) {
}
