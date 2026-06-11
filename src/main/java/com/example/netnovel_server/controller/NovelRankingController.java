package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelRankingDTO;
import com.example.netnovel_server.service.NovelRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
}
