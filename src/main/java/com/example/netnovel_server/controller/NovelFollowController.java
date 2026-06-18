package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelFollowDTO;
import com.example.netnovel_server.service.NovelFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/follows")
@Tag(name = "Follows", description = "Current user followed novel APIs")
public class NovelFollowController {

    private final NovelFollowService novelFollowService;

    public NovelFollowController(NovelFollowService novelFollowService) {
        this.novelFollowService = novelFollowService;
    }

    @GetMapping("/novels")
    @Operation(summary = "Get current user's followed novels")
    public ResponseEntity<Page<NovelFollowDTO>> getMyFollowedNovels(Pageable pageable) {
        return ResponseEntity.ok(novelFollowService.getMyFollowedNovels(pageable));
    }

    @GetMapping("/novels/{novelId}")
    @Operation(summary = "Get current user's follow record for a novel")
    public ResponseEntity<NovelFollowDTO> getMyFollowedNovel(@PathVariable Long novelId) {
        return ResponseEntity.ok(novelFollowService.getMyFollowedNovel(novelId));
    }

    @GetMapping("/novels/{novelId}/exists")
    @Operation(summary = "Check whether current user follows a novel")
    public ResponseEntity<Map<String, Boolean>> existsMyFollowedNovel(@PathVariable Long novelId) {
        return ResponseEntity.ok(Map.of("followed", novelFollowService.existsMyFollowedNovel(novelId)));
    }
}
