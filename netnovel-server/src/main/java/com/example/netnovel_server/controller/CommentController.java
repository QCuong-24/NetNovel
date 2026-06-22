package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.CommentCreateDTO;
import com.example.netnovel_server.dto.CommentDTO;
import com.example.netnovel_server.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Comments", description = "Novel, chapter, and reply comment APIs")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/novels/{novelId}/comments")
    @Operation(summary = "Get paginated root comments for a novel")
    public ResponseEntity<Page<CommentDTO>> getNovelComments(@PathVariable Long novelId, Pageable pageable) {
        return ResponseEntity.ok(commentService.getNovelComments(novelId, pageable));
    }

    @GetMapping("/api/chapters/{chapterId}/comments")
    @Operation(summary = "Get paginated root comments for a chapter")
    public ResponseEntity<Page<CommentDTO>> getChapterComments(@PathVariable Long chapterId, Pageable pageable) {
        return ResponseEntity.ok(commentService.getChapterComments(chapterId, pageable));
    }

    @GetMapping("/api/comments/{commentId}/replies")
    @Operation(summary = "Get direct replies for a comment")
    public ResponseEntity<List<CommentDTO>> getReplies(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getReplies(commentId));
    }

    @GetMapping("/api/comments/{commentId}/context")
    @Operation(summary = "Get a comment and its parent chain through the root comment")
    public ResponseEntity<List<CommentDTO>> getCommentContext(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getCommentContext(commentId));
    }

    @PostMapping("/api/novels/{novelId}/comments")
    @Operation(summary = "Create a root comment for a novel")
    public ResponseEntity<CommentDTO> createNovelComment(
        @PathVariable Long novelId,
        @RequestBody CommentCreateDTO request
    ) {
        return ResponseEntity.ok(commentService.createNovelComment(novelId, request));
    }

    @PostMapping("/api/chapters/{chapterId}/comments")
    @Operation(summary = "Create a root comment for a chapter")
    public ResponseEntity<CommentDTO> createChapterComment(
        @PathVariable Long chapterId,
        @RequestBody CommentCreateDTO request
    ) {
        return ResponseEntity.ok(commentService.createChapterComment(chapterId, request));
    }

    @PostMapping("/api/comments/{commentId}/replies")
    @Operation(summary = "Reply to a comment")
    public ResponseEntity<CommentDTO> createReply(
        @PathVariable Long commentId,
        @RequestBody CommentCreateDTO request
    ) {
        return ResponseEntity.ok(commentService.createReply(commentId, request));
    }

    @PutMapping("/api/comments/{commentId}")
    @Operation(summary = "Update own comment")
    public ResponseEntity<CommentDTO> updateComment(
        @PathVariable Long commentId,
        @RequestBody CommentCreateDTO request
    ) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    @DeleteMapping("/api/comments/{commentId}")
    @Operation(summary = "Soft delete own comment")
    public ResponseEntity<CommentDTO> deleteComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.deleteComment(commentId));
    }

    @DeleteMapping("/api/comments/{commentId}/moderation")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Soft delete a comment as manager or admin")
    public ResponseEntity<CommentDTO> moderateDeleteComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.moderateDeleteComment(commentId));
    }
}
