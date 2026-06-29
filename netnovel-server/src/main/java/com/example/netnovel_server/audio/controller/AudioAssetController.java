package com.example.netnovel_server.audio.controller;

import com.example.netnovel_server.audio.service.ChapterAudioService;
import com.example.netnovel_server.audio.dto.ChapterAudioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@Tag(name = "Audio Assets", description = "Generated audio file APIs")
public class AudioAssetController {

    private final ChapterAudioService chapterAudioService;

    public AudioAssetController(ChapterAudioService chapterAudioService) {
        this.chapterAudioService = chapterAudioService;
    }

    @GetMapping("/api/audio-assets/{assetId}")
    @Operation(summary = "Get a generated chapter audio asset status")
    public ResponseEntity<ChapterAudioResponseDTO> getAudioAsset(@PathVariable Long assetId) {
        return ResponseEntity.ok(chapterAudioService.getAudioAsset(assetId));
    }

    @GetMapping("/api/audio-assets/{assetId}/file")
    @Operation(summary = "Stream a generated chapter audio file")
    public ResponseEntity<Resource> streamAudio(@PathVariable Long assetId) {
        Resource resource = chapterAudioService.loadAudioFile(assetId);
        long contentLength = chapterAudioService.getAudioFileSize(assetId);
        chapterAudioService.markAccessed(assetId);

        return ResponseEntity.ok()
            .contentType(MediaType.valueOf("audio/mpeg"))
            .contentLength(contentLength)
            .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
            .body(resource);
    }
}
