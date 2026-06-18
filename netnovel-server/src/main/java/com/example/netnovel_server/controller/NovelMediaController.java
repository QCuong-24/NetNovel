package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.CloudinaryUploadSignatureDTO;
import com.example.netnovel_server.dto.ImageMetadataDTO;
import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.service.NovelMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/novels/{novelId}")
@Tag(name = "Novel Media", description = "Novel cover media APIs")
public class NovelMediaController {

    private final NovelMediaService novelMediaService;

    public NovelMediaController(NovelMediaService novelMediaService) {
        this.novelMediaService = novelMediaService;
    }

    @GetMapping("/cover/upload-signature")
    @Operation(summary = "Create a signed Cloudinary upload signature for novel cover")
    public ResponseEntity<CloudinaryUploadSignatureDTO> createCoverUploadSignature(@PathVariable Long novelId) {
        return ResponseEntity.ok(novelMediaService.createCoverUploadSignature(novelId));
    }

    @PatchMapping("/cover")
    @Operation(summary = "Save novel cover after frontend uploads it to Cloudinary")
    public ResponseEntity<NovelDTO> updateCover(
        @PathVariable Long novelId,
        @RequestBody ImageMetadataDTO request
    ) {
        return ResponseEntity.ok(novelMediaService.updateCover(novelId, request));
    }
}
