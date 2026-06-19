package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelInteractionDTO;
import com.example.netnovel_server.service.NovelInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/novels/{novelId}")
@Tag(name = "Novel Interactions", description = "Novel view, follow, like, and current user interaction APIs")
public class NovelInteractionController {

    private final NovelInteractionService novelInteractionService;

    public NovelInteractionController(NovelInteractionService novelInteractionService) {
        this.novelInteractionService = novelInteractionService;
    }

    @PostMapping("/view")
    @Operation(summary = "Increase novel view count")
    public ResponseEntity<NovelInteractionDTO> increaseView(@PathVariable Long novelId) {
        return ResponseEntity.ok(novelInteractionService.increaseView(novelId));
    }

    @PostMapping("/follow/toggle")
    @Operation(summary = "Toggle follow for current user")
    public ResponseEntity<NovelInteractionDTO> toggleFollow(@PathVariable Long novelId) {
        return ResponseEntity.ok(novelInteractionService.toggleFollow(novelId));
    }

    @PostMapping("/like/toggle")
    @Operation(summary = "Toggle like for current user")
    public ResponseEntity<NovelInteractionDTO> toggleLike(@PathVariable Long novelId) {
        return ResponseEntity.ok(novelInteractionService.toggleLike(novelId));
    }

    @PostMapping("/bookmark/toggle")
    @Operation(summary = "Toggle bookmark for current user")
    public ResponseEntity<NovelInteractionDTO> toggleBookmark(@PathVariable Long novelId) {
        return ResponseEntity.ok(novelInteractionService.toggleBookmark(novelId));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's interaction state for a novel")
    public ResponseEntity<NovelInteractionDTO> getMyInteraction(@PathVariable Long novelId) {
        return ResponseEntity.ok(novelInteractionService.getMyInteraction(novelId));
    }
}
