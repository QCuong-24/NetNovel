package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.NovelRankingDTO;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.repository.*;
import com.example.netnovel_server.utility.DateTimeUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class NovelRankingService {

    private final NovelViewRepository novelViewRepository;
    private final NovelFollowRepository novelFollowRepository;
    private final NovelLikeRepository novelLikeRepository;

    public NovelRankingService(
        NovelViewRepository novelViewRepository,
        NovelFollowRepository novelFollowRepository,
        NovelLikeRepository novelLikeRepository
    ) {
        this.novelViewRepository = novelViewRepository;
        this.novelFollowRepository = novelFollowRepository;
        this.novelLikeRepository = novelLikeRepository;
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopViewedByDay(LocalDate date, Pageable pageable) {
        return getTopViewedBetween(DateTimeUtils.startOfDay(date), DateTimeUtils.endOfDay(date), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopViewedByWeek(LocalDate date, Pageable pageable) {
        return getTopViewedBetween(DateTimeUtils.startOfWeek(date), DateTimeUtils.endOfWeek(date), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopViewedByMonth(YearMonth month, Pageable pageable) {
        return getTopViewedBetween(DateTimeUtils.startOfMonth(month), DateTimeUtils.endOfMonth(month), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopFollowedByDay(LocalDate date, Pageable pageable) {
        return getTopFollowedBetween(DateTimeUtils.startOfDay(date), DateTimeUtils.endOfDay(date), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopFollowedByWeek(LocalDate date, Pageable pageable) {
        return getTopFollowedBetween(DateTimeUtils.startOfWeek(date), DateTimeUtils.endOfWeek(date), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopFollowedByMonth(YearMonth month, Pageable pageable) {
        return getTopFollowedBetween(DateTimeUtils.startOfMonth(month), DateTimeUtils.endOfMonth(month), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopLikedByDay(LocalDate date, Pageable pageable) {
        return getTopLikedBetween(DateTimeUtils.startOfDay(date), DateTimeUtils.endOfDay(date), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopLikedByWeek(LocalDate date, Pageable pageable) {
        return getTopLikedBetween(DateTimeUtils.startOfWeek(date), DateTimeUtils.endOfWeek(date), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopLikedByMonth(YearMonth month, Pageable pageable) {
        return getTopLikedBetween(DateTimeUtils.startOfMonth(month), DateTimeUtils.endOfMonth(month), pageable);
    }

    private Page<NovelRankingDTO> getTopViewedBetween(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return novelViewRepository.findTopViewedNovelsBetween(start, end, pageable)
            .map(item -> toRankingDTO(item.getNovel(), item.getViewCount()));
    }

    private Page<NovelRankingDTO> getTopFollowedBetween(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return novelFollowRepository.findTopFollowedNovelsBetween(start, end, pageable)
            .map(item -> toRankingDTO(item.getNovel(), item.getFollowCount()));
    }

    private Page<NovelRankingDTO> getTopLikedBetween(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return novelLikeRepository.findTopLikedNovelsBetween(start, end, pageable)
            .map(item -> toRankingDTO(item.getNovel(), item.getLikeCount()));
    }

    private NovelRankingDTO toRankingDTO(com.example.netnovel_server.entity.Novel novel, Long count) {
        return NovelRankingDTO.builder()
            .novel(NovelMapper.toDTO(novel))
            .count(count)
            .build();
    }
}
