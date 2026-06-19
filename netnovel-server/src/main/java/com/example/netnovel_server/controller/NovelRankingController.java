package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelRankingDTO;
import com.example.netnovel_server.dto.NovelStatisticDTO;
import com.example.netnovel_server.service.NovelRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/novel-rankings")
@Tag(name = "Novel Rankings", description = "Novel ranking APIs by views, follows, and likes")
public class NovelRankingController {

    private final NovelRankingService novelRankingService;

    public NovelRankingController(NovelRankingService novelRankingService) {
        this.novelRankingService = novelRankingService;
    }

    @GetMapping("/views/day")
    @Operation(summary = "Get daily top viewed novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopViewedByDay(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopViewedByDay(date, pageable));
    }

    @GetMapping("/views/week")
    @Operation(summary = "Get weekly top viewed novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopViewedByWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopViewedByWeek(date, pageable));
    }

    @GetMapping("/views/month")
    @Operation(summary = "Get monthly top viewed novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopViewedByMonth(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopViewedByMonth(month, pageable));
    }

    @GetMapping("/views/year")
    @Operation(summary = "Get yearly top viewed novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopViewedByYear(
        @RequestParam @DateTimeFormat(pattern = "yyyy") Year year,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopViewedByYear(year, pageable));
    }

    @GetMapping("/follows/day")
    @Operation(summary = "Get daily top followed novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopFollowedByDay(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopFollowedByDay(date, pageable));
    }

    @GetMapping("/follows/week")
    @Operation(summary = "Get weekly top followed novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopFollowedByWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopFollowedByWeek(date, pageable));
    }

    @GetMapping("/follows/month")
    @Operation(summary = "Get monthly top followed novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopFollowedByMonth(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopFollowedByMonth(month, pageable));
    }

    @GetMapping("/follows/year")
    @Operation(summary = "Get yearly top followed novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopFollowedByYear(
        @RequestParam @DateTimeFormat(pattern = "yyyy") Year year,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopFollowedByYear(year, pageable));
    }

    @GetMapping("/likes/day")
    @Operation(summary = "Get daily top liked novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopLikedByDay(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopLikedByDay(date, pageable));
    }

    @GetMapping("/likes/week")
    @Operation(summary = "Get weekly top liked novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopLikedByWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopLikedByWeek(date, pageable));
    }

    @GetMapping("/likes/month")
    @Operation(summary = "Get monthly top liked novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopLikedByMonth(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopLikedByMonth(month, pageable));
    }

    @GetMapping("/likes/year")
    @Operation(summary = "Get yearly top liked novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopLikedByYear(
        @RequestParam @DateTimeFormat(pattern = "yyyy") Year year,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopLikedByYear(year, pageable));
    }

    @GetMapping("/bookmarks/day")
    @Operation(summary = "Get daily top bookmarked novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopBookmarkedByDay(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopBookmarkedByDay(date, pageable));
    }

    @GetMapping("/bookmarks/week")
    @Operation(summary = "Get weekly top bookmarked novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopBookmarkedByWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopBookmarkedByWeek(date, pageable));
    }

    @GetMapping("/bookmarks/month")
    @Operation(summary = "Get monthly top bookmarked novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopBookmarkedByMonth(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopBookmarkedByMonth(month, pageable));
    }

    @GetMapping("/bookmarks/year")
    @Operation(summary = "Get yearly top bookmarked novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopBookmarkedByYear(
        @RequestParam @DateTimeFormat(pattern = "yyyy") Year year,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopBookmarkedByYear(year, pageable));
    }

    @GetMapping("/comments/day")
    @Operation(summary = "Get daily top commented novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopCommentedByDay(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopCommentedByDay(date, pageable));
    }

    @GetMapping("/comments/week")
    @Operation(summary = "Get weekly top commented novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopCommentedByWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopCommentedByWeek(date, pageable));
    }

    @GetMapping("/comments/month")
    @Operation(summary = "Get monthly top commented novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopCommentedByMonth(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopCommentedByMonth(month, pageable));
    }

    @GetMapping("/comments/year")
    @Operation(summary = "Get yearly top commented novels")
    public ResponseEntity<Page<NovelRankingDTO>> getTopCommentedByYear(
        @RequestParam @DateTimeFormat(pattern = "yyyy") Year year,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelRankingService.getTopCommentedByYear(year, pageable));
    }

    @GetMapping("/views/total/day")
    @Operation(summary = "Get daily total novel views")
    public ResponseEntity<NovelStatisticDTO> getTotalViewsByDay(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalViewsByDay(date));
    }

    @GetMapping("/views/total/week")
    @Operation(summary = "Get weekly total novel views")
    public ResponseEntity<NovelStatisticDTO> getTotalViewsByWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalViewsByWeek(date));
    }

    @GetMapping("/views/total/month")
    @Operation(summary = "Get monthly total novel views")
    public ResponseEntity<NovelStatisticDTO> getTotalViewsByMonth(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalViewsByMonth(month));
    }

    @GetMapping("/views/total/year")
    @Operation(summary = "Get yearly total novel views")
    public ResponseEntity<NovelStatisticDTO> getTotalViewsByYear(
        @RequestParam @DateTimeFormat(pattern = "yyyy") Year year
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalViewsByYear(year));
    }

    @GetMapping("/follows/total/day")
    @Operation(summary = "Get daily total novel follows")
    public ResponseEntity<NovelStatisticDTO> getTotalFollowsByDay(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalFollowsByDay(date));
    }

    @GetMapping("/follows/total/week")
    @Operation(summary = "Get weekly total novel follows")
    public ResponseEntity<NovelStatisticDTO> getTotalFollowsByWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalFollowsByWeek(date));
    }

    @GetMapping("/follows/total/month")
    @Operation(summary = "Get monthly total novel follows")
    public ResponseEntity<NovelStatisticDTO> getTotalFollowsByMonth(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalFollowsByMonth(month));
    }

    @GetMapping("/follows/total/year")
    @Operation(summary = "Get yearly total novel follows")
    public ResponseEntity<NovelStatisticDTO> getTotalFollowsByYear(
        @RequestParam @DateTimeFormat(pattern = "yyyy") Year year
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalFollowsByYear(year));
    }

    @GetMapping("/likes/total/day")
    @Operation(summary = "Get daily total novel likes")
    public ResponseEntity<NovelStatisticDTO> getTotalLikesByDay(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalLikesByDay(date));
    }

    @GetMapping("/likes/total/week")
    @Operation(summary = "Get weekly total novel likes")
    public ResponseEntity<NovelStatisticDTO> getTotalLikesByWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalLikesByWeek(date));
    }

    @GetMapping("/likes/total/month")
    @Operation(summary = "Get monthly total novel likes")
    public ResponseEntity<NovelStatisticDTO> getTotalLikesByMonth(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalLikesByMonth(month));
    }

    @GetMapping("/likes/total/year")
    @Operation(summary = "Get yearly total novel likes")
    public ResponseEntity<NovelStatisticDTO> getTotalLikesByYear(
        @RequestParam @DateTimeFormat(pattern = "yyyy") Year year
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalLikesByYear(year));
    }

    @GetMapping("/bookmarks/total/day")
    @Operation(summary = "Get daily total novel bookmarks")
    public ResponseEntity<NovelStatisticDTO> getTotalBookmarksByDay(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalBookmarksByDay(date));
    }

    @GetMapping("/bookmarks/total/week")
    @Operation(summary = "Get weekly total novel bookmarks")
    public ResponseEntity<NovelStatisticDTO> getTotalBookmarksByWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalBookmarksByWeek(date));
    }

    @GetMapping("/bookmarks/total/month")
    @Operation(summary = "Get monthly total novel bookmarks")
    public ResponseEntity<NovelStatisticDTO> getTotalBookmarksByMonth(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalBookmarksByMonth(month));
    }

    @GetMapping("/bookmarks/total/year")
    @Operation(summary = "Get yearly total novel bookmarks")
    public ResponseEntity<NovelStatisticDTO> getTotalBookmarksByYear(
        @RequestParam @DateTimeFormat(pattern = "yyyy") Year year
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalBookmarksByYear(year));
    }

    @GetMapping("/comments/total/day")
    @Operation(summary = "Get daily total novel comments")
    public ResponseEntity<NovelStatisticDTO> getTotalCommentsByDay(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalCommentsByDay(date));
    }

    @GetMapping("/comments/total/week")
    @Operation(summary = "Get weekly total novel comments")
    public ResponseEntity<NovelStatisticDTO> getTotalCommentsByWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalCommentsByWeek(date));
    }

    @GetMapping("/comments/total/month")
    @Operation(summary = "Get monthly total novel comments")
    public ResponseEntity<NovelStatisticDTO> getTotalCommentsByMonth(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalCommentsByMonth(month));
    }

    @GetMapping("/comments/total/year")
    @Operation(summary = "Get yearly total novel comments")
    public ResponseEntity<NovelStatisticDTO> getTotalCommentsByYear(
        @RequestParam @DateTimeFormat(pattern = "yyyy") Year year
    ) {
        return ResponseEntity.ok(novelRankingService.getTotalCommentsByYear(year));
    }
}
