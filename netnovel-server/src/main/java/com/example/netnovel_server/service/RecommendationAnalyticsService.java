package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.UserEventDataReportDTO;
import com.example.netnovel_server.entity.UserEventType;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.repository.UserEventInteractionCountProjection;
import com.example.netnovel_server.repository.UserEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationAnalyticsService {

    private static final int MIN_REPORT_DAYS = 1;
    private static final int MAX_REPORT_DAYS = 365;

    private final UserEventRepository userEventRepository;

    public RecommendationAnalyticsService(UserEventRepository userEventRepository) {
        this.userEventRepository = userEventRepository;
    }

    @Transactional(readOnly = true)
    public UserEventDataReportDTO getUserEventReport(int periodDays) {
        if (periodDays < MIN_REPORT_DAYS || periodDays > MAX_REPORT_DAYS) {
            throw new BadRequestException("days must be between " + MIN_REPORT_DAYS + " and " + MAX_REPORT_DAYS);
        }

        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(periodDays);
        Map<UserEventType, Long> eventsByType = new EnumMap<>(UserEventType.class);
        for (UserEventType eventType : UserEventType.values()) {
            eventsByType.put(eventType, 0L);
        }
        userEventRepository.countEventsByType(from, to)
            .forEach(count -> eventsByType.put(count.getEventType(), count.getEventCount()));

        List<UserEventInteractionCountProjection> novelsPerUser = userEventRepository.countDistinctNovelsByUser(from, to);
        List<UserEventInteractionCountProjection> usersPerNovel = userEventRepository.countDistinctUsersByNovel(from, to);

        return new UserEventDataReportDTO(
            periodDays,
            from,
            to,
            userEventRepository.countByEventAtBetween(from, to),
            eventsByType,
            userEventRepository.countActiveUsersWithNovelInteractions(from, to),
            userEventRepository.countInteractedNovels(from, to),
            countAtLeast(novelsPerUser, 3),
            countAtLeast(novelsPerUser, 5),
            countAtLeast(usersPerNovel, 3),
            countAtLeast(usersPerNovel, 5),
            averageDistinctCount(novelsPerUser),
            averageDistinctCount(usersPerNovel)
        );
    }

    private long countAtLeast(List<UserEventInteractionCountProjection> counts, long threshold) {
        return counts.stream()
            .filter(count -> count.getDistinctCount() >= threshold)
            .count();
    }

    private double averageDistinctCount(List<UserEventInteractionCountProjection> counts) {
        return counts.stream()
            .mapToLong(UserEventInteractionCountProjection::getDistinctCount)
            .average()
            .orElse(0.0);
    }
}
