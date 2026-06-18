package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelLastReadDTO;
import com.example.netnovel_server.dto.NovelLastReadUpdateDTO;
import com.example.netnovel_server.service.NovelLastReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/last-reads")
@Tag(name = "Last Reads", description = "Current user last-read APIs")
public class NovelLastReadController {

    private final NovelLastReadService novelLastReadService;

    public NovelLastReadController(NovelLastReadService novelLastReadService) {
        this.novelLastReadService = novelLastReadService;
    }

    @GetMapping
    @Operation(summary = "Get current user's recently read novels")
    public ResponseEntity<Page<NovelLastReadDTO>> getMyLastReads(Pageable pageable) {
        return ResponseEntity.ok(novelLastReadService.getMyLastReads(pageable));
    }

    @GetMapping("/novels/{novelId}")
    @Operation(summary = "Get current user's last read for a novel")
    public ResponseEntity<NovelLastReadDTO> getMyNovelLastRead(@PathVariable Long novelId) {
        return ResponseEntity.ok(novelLastReadService.getMyNovelLastRead(novelId));
    }

    @PostMapping
    @Operation(summary = "Create or update current user's last read")
    public ResponseEntity<NovelLastReadDTO> updateMyLastRead(@RequestBody NovelLastReadUpdateDTO request) {
        return ResponseEntity.ok(novelLastReadService.updateMyLastRead(request));
    }

    @PutMapping("/novels/{novelId}/chapters/{chapterId}")
    @Operation(summary = "Update current user's last read for a novel")
    public ResponseEntity<NovelLastReadDTO> updateMyNovelLastRead(
        @PathVariable Long novelId,
        @PathVariable Long chapterId
    ) {
        return ResponseEntity.ok(novelLastReadService.updateMyNovelLastRead(novelId, chapterId));
    }

    @DeleteMapping("/novels/{novelId}")
    @Operation(summary = "Delete current user's last read for a novel")
    public ResponseEntity<Void> deleteMyNovelLastRead(@PathVariable Long novelId) {
        novelLastReadService.deleteMyNovelLastRead(novelId);
        return ResponseEntity.noContent().build();
    }
}
