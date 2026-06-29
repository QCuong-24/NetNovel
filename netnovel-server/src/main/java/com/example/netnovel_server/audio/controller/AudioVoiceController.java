package com.example.netnovel_server.audio.controller;

import com.example.netnovel_server.audio.dto.AudioVoiceDTO;
import com.example.netnovel_server.audio.service.AudioVoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Audio Voices", description = "Public audio voice catalog APIs")
public class AudioVoiceController {

    private final AudioVoiceService audioVoiceService;

    public AudioVoiceController(AudioVoiceService audioVoiceService) {
        this.audioVoiceService = audioVoiceService;
    }

    @GetMapping("/api/audio/voices")
    @Operation(summary = "List enabled audio voices")
    public ResponseEntity<List<AudioVoiceDTO>> getVoices(@RequestParam(required = false) String languageCode) {
        return ResponseEntity.ok(audioVoiceService.getEnabledVoices(languageCode));
    }
}
