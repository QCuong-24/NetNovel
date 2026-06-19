package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.NovelRankingDTO;
import com.example.netnovel_server.dto.NovelStatisticDTO;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.repository.*;
import com.example.netnovel_server.utility.DateTimeUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;

@Service
public class NovelRankingService {

    private final NovelViewStatRepository novelViewStatRepository;
    private final NovelFollowRepository novelFollowRepository;
    private final NovelLikeRepository novelLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CommentRepository commentRepository;

    public NovelRankingService(
        NovelViewStatRepository novelViewStatRepository,
        NovelFollowRepository novelFollowRepository,
        NovelLikeRepository novelLikeRepository,
        BookmarkRepository bookmarkRepository,
        CommentRepository commentRepository
    ) {
        this.novelViewStatRepository = novelViewStatRepository;
        this.novelFollowRepository = novelFollowRepository;
        this.novelLikeRepository = novelLikeRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopViewedByDay(LocalDate date, Pageable pageable) {
        return getTopViewedBetween(date, date, pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopViewedByWeek(LocalDate date, Pageable pageable) {
        return getTopViewedBetween(
            DateTimeUtils.startOfWeek(date).toLocalDate(),
            DateTimeUtils.endOfWeek(date).toLocalDate(),
            pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopViewedByMonth(YearMonth month, Pageable pageable) {
        return getTopViewedBetween(month.atDay(1), month.atEndOfMonth(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopViewedByYear(Year year, Pageable pageable) {
        return getTopViewedBetween(year.atDay(1), year.atMonth(12).atEndOfMonth(), pageable);
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
    public Page<NovelRankingDTO> getTopFollowedByYear(Year year, Pageable pageable) {
        return getTopFollowedBetween(DateTimeUtils.startOfYear(year), DateTimeUtils.endOfYear(year), pageable);
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

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopLikedByYear(Year year, Pageable pageable) {
        return getTopLikedBetween(DateTimeUtils.startOfYear(year), DateTimeUtils.endOfYear(year), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopBookmarkedByDay(LocalDate date, Pageable pageable) {
        return getTopBookmarkedBetween(DateTimeUtils.startOfDay(date), DateTimeUtils.endOfDay(date), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopBookmarkedByWeek(LocalDate date, Pageable pageable) {
        return getTopBookmarkedBetween(DateTimeUtils.startOfWeek(date), DateTimeUtils.endOfWeek(date), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopBookmarkedByMonth(YearMonth month, Pageable pageable) {
        return getTopBookmarkedBetween(DateTimeUtils.startOfMonth(month), DateTimeUtils.endOfMonth(month), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopBookmarkedByYear(Year year, Pageable pageable) {
        return getTopBookmarkedBetween(DateTimeUtils.startOfYear(year), DateTimeUtils.endOfYear(year), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopCommentedByDay(LocalDate date, Pageable pageable) {
        return getTopCommentedBetween(DateTimeUtils.startOfDay(date), DateTimeUtils.endOfDay(date), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopCommentedByWeek(LocalDate date, Pageable pageable) {
        return getTopCommentedBetween(DateTimeUtils.startOfWeek(date), DateTimeUtils.endOfWeek(date), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopCommentedByMonth(YearMonth month, Pageable pageable) {
        return getTopCommentedBetween(DateTimeUtils.startOfMonth(month), DateTimeUtils.endOfMonth(month), pageable);
    }

    @Transactional(readOnly = true)
    public Page<NovelRankingDTO> getTopCommentedByYear(Year year, Pageable pageable) {
        return getTopCommentedBetween(DateTimeUtils.startOfYear(year), DateTimeUtils.endOfYear(year), pageable);
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalViewsByDay(LocalDate date) {
        return buildStatistic("views", "day", date, date, novelViewStatRepository.countTotalViewsBetween(date, date));
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalViewsByWeek(LocalDate date) {
        LocalDate start = DateTimeUtils.startOfWeek(date).toLocalDate();
        LocalDate end = DateTimeUtils.endOfWeek(date).toLocalDate();
        return buildStatistic("views", "week", start, end, novelViewStatRepository.countTotalViewsBetween(start, end));
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalViewsByMonth(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return buildStatistic("views", "month", start, end, novelViewStatRepository.countTotalViewsBetween(start, end));
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalViewsByYear(Year year) {
        LocalDate start = year.atDay(1);
        LocalDate end = year.atMonth(12).atEndOfMonth();
        return buildStatistic("views", "year", start, end, novelViewStatRepository.countTotalViewsBetween(start, end));
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalFollowsByDay(LocalDate date) {
        return getTotalFollowsBetween("day", date, date);
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalFollowsByWeek(LocalDate date) {
        LocalDate start = DateTimeUtils.startOfWeek(date).toLocalDate();
        LocalDate end = DateTimeUtils.endOfWeek(date).toLocalDate();
        return getTotalFollowsBetween("week", start, end);
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalFollowsByMonth(YearMonth month) {
        return getTotalFollowsBetween("month", month.atDay(1), month.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalFollowsByYear(Year year) {
        return getTotalFollowsBetween("year", year.atDay(1), year.atMonth(12).atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalLikesByDay(LocalDate date) {
        return getTotalLikesBetween("day", date, date);
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalLikesByWeek(LocalDate date) {
        LocalDate start = DateTimeUtils.startOfWeek(date).toLocalDate();
        LocalDate end = DateTimeUtils.endOfWeek(date).toLocalDate();
        return getTotalLikesBetween("week", start, end);
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalLikesByMonth(YearMonth month) {
        return getTotalLikesBetween("month", month.atDay(1), month.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalLikesByYear(Year year) {
        return getTotalLikesBetween("year", year.atDay(1), year.atMonth(12).atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalBookmarksByDay(LocalDate date) {
        return getTotalBookmarksBetween("day", date, date);
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalBookmarksByWeek(LocalDate date) {
        LocalDate start = DateTimeUtils.startOfWeek(date).toLocalDate();
        LocalDate end = DateTimeUtils.endOfWeek(date).toLocalDate();
        return getTotalBookmarksBetween("week", start, end);
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalBookmarksByMonth(YearMonth month) {
        return getTotalBookmarksBetween("month", month.atDay(1), month.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalBookmarksByYear(Year year) {
        return getTotalBookmarksBetween("year", year.atDay(1), year.atMonth(12).atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalCommentsByDay(LocalDate date) {
        return getTotalCommentsBetween("day", date, date);
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalCommentsByWeek(LocalDate date) {
        LocalDate start = DateTimeUtils.startOfWeek(date).toLocalDate();
        LocalDate end = DateTimeUtils.endOfWeek(date).toLocalDate();
        return getTotalCommentsBetween("week", start, end);
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalCommentsByMonth(YearMonth month) {
        return getTotalCommentsBetween("month", month.atDay(1), month.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public NovelStatisticDTO getTotalCommentsByYear(Year year) {
        return getTotalCommentsBetween("year", year.atDay(1), year.atMonth(12).atEndOfMonth());
    }

    private Page<NovelRankingDTO> getTopViewedBetween(LocalDate start, LocalDate end, Pageable pageable) {
        return novelViewStatRepository.findTopViewedNovelsBetween(start, end, pageable)
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

    private Page<NovelRankingDTO> getTopBookmarkedBetween(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return bookmarkRepository.findTopBookmarkedNovelsBetween(start, end, pageable)
            .map(item -> toRankingDTO(item.getNovel(), item.getBookmarkCount()));
    }

    private Page<NovelRankingDTO> getTopCommentedBetween(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return commentRepository.findTopCommentedNovelsBetween(start, end, pageable)
            .map(item -> toRankingDTO(item.getNovel(), item.getCommentCount()));
    }

    private NovelStatisticDTO getTotalFollowsBetween(String period, LocalDate start, LocalDate end) {
        long count = novelFollowRepository.countTotalFollowsBetween(start.atStartOfDay(), end.atTime(java.time.LocalTime.MAX));
        return buildStatistic("follows", period, start, end, count);
    }

    private NovelStatisticDTO getTotalLikesBetween(String period, LocalDate start, LocalDate end) {
        long count = novelLikeRepository.countTotalLikesBetween(start.atStartOfDay(), end.atTime(java.time.LocalTime.MAX));
        return buildStatistic("likes", period, start, end, count);
    }

    private NovelStatisticDTO getTotalBookmarksBetween(String period, LocalDate start, LocalDate end) {
        long count = bookmarkRepository.countTotalBookmarksBetween(start.atStartOfDay(), end.atTime(java.time.LocalTime.MAX));
        return buildStatistic("bookmarks", period, start, end, count);
    }

    private NovelStatisticDTO getTotalCommentsBetween(String period, LocalDate start, LocalDate end) {
        long count = commentRepository.countTotalCommentsBetween(start.atStartOfDay(), end.atTime(java.time.LocalTime.MAX));
        return buildStatistic("comments", period, start, end, count);
    }

    private NovelRankingDTO toRankingDTO(com.example.netnovel_server.entity.Novel novel, Long count) {
        return NovelRankingDTO.builder()
            .novel(NovelMapper.toDTO(novel))
            .count(count)
            .build();
    }

    private NovelStatisticDTO buildStatistic(String metric, String period, LocalDate start, LocalDate end, Long count) {
        return NovelStatisticDTO.builder()
            .metric(metric)
            .period(period)
            .startDate(start)
            .endDate(end)
            .count(count)
            .build();
    }
}
