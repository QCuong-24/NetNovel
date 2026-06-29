package com.example.netnovel_server.audio.controller;

import com.example.netnovel_server.audio.dto.ChapterAudioRequestDTO;
import com.example.netnovel_server.audio.dto.ChapterAudioResponseDTO;
import com.example.netnovel_server.audio.service.ChapterAudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Chapter Audio", description = "Chapter text-to-speech generation and cache APIs")
public class ChapterAudioController {

    private final ChapterAudioService chapterAudioService;

    public ChapterAudioController(ChapterAudioService chapterAudioService) {
        this.chapterAudioService = chapterAudioService;
    }

    @PostMapping("/api/chapters/{chapterId}/audio")
    @Operation(summary = "Create or fetch cached audio for a chapter")
    public ResponseEntity<ChapterAudioResponseDTO> createChapterAudio(
        @PathVariable Long chapterId,
        @RequestBody(required = false) ChapterAudioRequestDTO request
    ) {
        return ResponseEntity.ok(chapterAudioService.createOrGetChapterAudio(chapterId, request));
    }
}
