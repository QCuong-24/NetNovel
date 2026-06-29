package com.example.netnovel_server.audio.controller;

import com.example.netnovel_server.audio.dto.*;
import com.example.netnovel_server.audio.entity.ChapterAudioStatus;
import com.example.netnovel_server.audio.service.AdminAudioService;
import com.example.netnovel_server.audio.service.AudioVoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audio")
@Tag(name = "Admin Audio", description = "Audio generation, cache, voice, and storage administration APIs")
public class AdminAudioController {

    private final AdminAudioService adminAudioService;
    private final AudioVoiceService audioVoiceService;

    public AdminAudioController(AdminAudioService adminAudioService, AudioVoiceService audioVoiceService) {
        this.adminAudioService = adminAudioService;
        this.audioVoiceService = audioVoiceService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get audio dashboard metrics")
    public ResponseEntity<AdminAudioDashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminAudioService.getDashboard());
    }

    @GetMapping("/assets")
    @Operation(summary = "List generated audio assets")
    public ResponseEntity<Page<AdminAudioAssetDTO>> getAssets(
        @RequestParam(required = false) ChapterAudioStatus status,
        Pageable pageable
    ) {
        return ResponseEntity.ok(adminAudioService.getAssets(status, pageable));
    }

    @PostMapping("/assets/{assetId}/retry")
    @Operation(summary = "Retry a failed audio asset")
    public ResponseEntity<Void> retryAsset(@PathVariable Long assetId) {
        adminAudioService.retry(assetId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/assets/{assetId}")
    @Operation(summary = "Delete an audio asset and its stored file")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long assetId) {
        adminAudioService.delete(assetId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cleanup-expired")
    @Operation(summary = "Delete expired audio assets")
    public ResponseEntity<Map<String, Integer>> cleanupExpired() {
        return ResponseEntity.ok(Map.of("deleted", adminAudioService.cleanupExpired()));
    }

    @GetMapping("/voices")
    @Operation(summary = "List audio voices for admins")
    public ResponseEntity<List<AudioVoiceDTO>> getVoices() {
        return ResponseEntity.ok(audioVoiceService.getAdminVoices());
    }

    @PatchMapping("/voices/{voiceId}")
    @Operation(summary = "Update an audio voice")
    public ResponseEntity<AudioVoiceDTO> updateVoice(
        @PathVariable Long voiceId,
        @RequestBody AudioVoiceUpdateDTO request
    ) {
        return ResponseEntity.ok(audioVoiceService.updateVoice(voiceId, request));
    }

    @PostMapping("/voices/sync")
    @Operation(summary = "Sync AWS Polly voices into the voice catalog")
    public ResponseEntity<List<AudioVoiceDTO>> syncVoices() {
        return ResponseEntity.ok(audioVoiceService.syncPollyVoices());
    }
}
