package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelCreateDTO;
import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.service.NovelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/novels")
@Tag(name = "Novels", description = "Novel browsing and management APIs")
public class NovelController {

    private final NovelService novelService;

    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @GetMapping
    @Operation(summary = "Get novels", description = "Returns a paginated list of novels.")
    public ResponseEntity<Page<NovelDTO>> getNovels(Pageable pageable) {
        return ResponseEntity.ok(novelService.getNovels(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search novels by title")
    public ResponseEntity<Page<NovelDTO>> searchByTitle(@RequestParam String title, Pageable pageable) {
        return ResponseEntity.ok(novelService.searchByTitle(title, pageable));
    }

    @GetMapping("/latest-updates")
    @Operation(summary = "Get latest updated novels")
    public ResponseEntity<Page<NovelDTO>> getLatestUpdatedNovels(Pageable pageable) {
        return ResponseEntity.ok(novelService.getLatestUpdatedNovels(pageable));
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed novels")
    public ResponseEntity<Page<NovelDTO>> getCompletedNovels(Pageable pageable) {
        return ResponseEntity.ok(novelService.getCompletedNovels(pageable));
    }

    @GetMapping("/updated")
    @Operation(summary = "Get novels updated in a time range")
    public ResponseEntity<Page<NovelDTO>> getUpdatedNovels(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
        Pageable pageable
    ) {
        return ResponseEntity.ok(novelService.getUpdatedNovels(start, end, pageable));
    }

    @GetMapping("/{novelId}")
    @Operation(summary = "Get a novel by id")
    public ResponseEntity<NovelDTO> getNovel(@PathVariable Long novelId) {
        return ResponseEntity.ok(novelService.getNovel(novelId));
    }

    @PostMapping
    @Operation(summary = "Create a novel")
    public ResponseEntity<NovelDTO> createNovel(@RequestBody NovelCreateDTO request) {
        return ResponseEntity.ok(novelService.createNovel(request));
    }

    @PutMapping("/{novelId}")
    @Operation(summary = "Update a novel")
    public ResponseEntity<NovelDTO> updateNovel(@PathVariable Long novelId, @RequestBody NovelCreateDTO request) {
        return ResponseEntity.ok(novelService.updateNovel(novelId, request));
    }

    @DeleteMapping("/{novelId}")
    @Operation(summary = "Delete a novel")
    public ResponseEntity<Void> deleteNovel(@PathVariable Long novelId) {
        novelService.deleteNovel(novelId);
        return ResponseEntity.noContent().build();
    }
}
