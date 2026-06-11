package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.ChapterContentDTO;
import com.example.netnovel_server.dto.ChapterCreateDTO;
import com.example.netnovel_server.dto.ChapterDTO;
import com.example.netnovel_server.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Chapters", description = "Chapter list, content, and management APIs")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @GetMapping("/api/novels/{novelId}/chapters")
    @Operation(summary = "Get paginated chapter list for a novel")
    public ResponseEntity<Page<ChapterDTO>> getChapters(@PathVariable Long novelId, Pageable pageable) {
        return ResponseEntity.ok(chapterService.getChapters(novelId, pageable));
    }

    @GetMapping("/api/novels/{novelId}/chapters/all")
    @Operation(summary = "Get all chapters for a novel")
    public ResponseEntity<List<ChapterDTO>> getAllChapters(@PathVariable Long novelId) {
        return ResponseEntity.ok(chapterService.getAllChapters(novelId));
    }

    @GetMapping("/api/chapters/{chapterId}")
    @Operation(summary = "Get chapter content by id")
    public ResponseEntity<ChapterContentDTO> getChapter(@PathVariable Long chapterId) {
        return ResponseEntity.ok(chapterService.getChapter(chapterId));
    }

    @PostMapping("/api/novels/{novelId}/chapters")
    @Operation(summary = "Create a chapter for a novel")
    public ResponseEntity<ChapterContentDTO> createChapter(
        @PathVariable Long novelId,
        @RequestBody ChapterCreateDTO request
    ) {
        return ResponseEntity.ok(chapterService.createChapter(novelId, request));
    }

    @PutMapping("/api/chapters/{chapterId}")
    @Operation(summary = "Update chapter title, number, and content")
    public ResponseEntity<ChapterContentDTO> updateChapter(
        @PathVariable Long chapterId,
        @RequestBody ChapterCreateDTO request
    ) {
        return ResponseEntity.ok(chapterService.updateChapter(chapterId, request));
    }

    @DeleteMapping("/api/chapters/{chapterId}")
    @Operation(summary = "Delete a chapter")
    public ResponseEntity<Void> deleteChapter(@PathVariable Long chapterId) {
        chapterService.deleteChapter(chapterId);
        return ResponseEntity.noContent().build();
    }
}
