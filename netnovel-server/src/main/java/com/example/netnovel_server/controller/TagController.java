package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.TagDTO;
import com.example.netnovel_server.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "Tags", description = "Tag catalog APIs")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    @Operation(summary = "Get all tags")
    public ResponseEntity<List<TagDTO>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/{tagId}")
    @Operation(summary = "Get a tag by id")
    public ResponseEntity<TagDTO> getTag(@PathVariable Long tagId) {
        return ResponseEntity.ok(tagService.getTag(tagId));
    }

    @GetMapping("/novels/{novelId}")
    @Operation(summary = "Get tags of a novel")
    public ResponseEntity<List<TagDTO>> getNovelTags(@PathVariable Long novelId) {
        return ResponseEntity.ok(tagService.getNovelTags(novelId));
    }

    @PostMapping
    @Operation(summary = "Create a tag")
    public ResponseEntity<TagDTO> createTag(@RequestBody TagDTO request) {
        return ResponseEntity.ok(tagService.createTag(request));
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple tags from a string list")
    public ResponseEntity<List<TagDTO>> createTags(@RequestBody List<String> tagNames) {
        return ResponseEntity.ok(tagService.createTags(tagNames));
    }

    @PutMapping("/novels/{novelId}")
    @Operation(summary = "Update tags of a novel")
    public ResponseEntity<List<TagDTO>> updateNovelTags(
        @PathVariable Long novelId,
        @RequestBody List<String> tagNames
    ) {
        return ResponseEntity.ok(tagService.updateNovelTags(novelId, tagNames));
    }

    @DeleteMapping("/{tagId}")
    @Operation(summary = "Delete a tag and remove it from related novels")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}
