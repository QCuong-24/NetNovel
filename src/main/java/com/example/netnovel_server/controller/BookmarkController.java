package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.BookmarkCreateDTO;
import com.example.netnovel_server.dto.BookmarkDTO;
import com.example.netnovel_server.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@Tag(name = "Bookmarks", description = "Current user bookmark APIs")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @GetMapping
    @Operation(summary = "Get current user's bookmarks")
    public ResponseEntity<Page<BookmarkDTO>> getMyBookmarks(Pageable pageable) {
        return ResponseEntity.ok(bookmarkService.getMyBookmarks(pageable));
    }

    @GetMapping("/novels")
    @Operation(summary = "Get current user's novel bookmarks")
    public ResponseEntity<Page<BookmarkDTO>> getMyNovelBookmarks(Pageable pageable) {
        return ResponseEntity.ok(bookmarkService.getMyNovelBookmarks(pageable));
    }

    @GetMapping("/chapters")
    @Operation(summary = "Get current user's chapter bookmarks")
    public ResponseEntity<Page<BookmarkDTO>> getMyChapterBookmarks(Pageable pageable) {
        return ResponseEntity.ok(bookmarkService.getMyChapterBookmarks(pageable));
    }

    @GetMapping("/{bookmarkId}")
    @Operation(summary = "Get current user's bookmark by id")
    public ResponseEntity<BookmarkDTO> getMyBookmark(@PathVariable Long bookmarkId) {
        return ResponseEntity.ok(bookmarkService.getMyBookmark(bookmarkId));
    }

    @GetMapping("/novels/{novelId}")
    @Operation(summary = "Get current user's bookmark for a novel")
    public ResponseEntity<BookmarkDTO> getMyNovelBookmark(@PathVariable Long novelId) {
        return ResponseEntity.ok(bookmarkService.getMyNovelBookmark(novelId));
    }

    @GetMapping("/chapters/{chapterId}")
    @Operation(summary = "Get current user's bookmark for a chapter")
    public ResponseEntity<BookmarkDTO> getMyChapterBookmark(@PathVariable Long chapterId) {
        return ResponseEntity.ok(bookmarkService.getMyChapterBookmark(chapterId));
    }

    @GetMapping("/novels/{novelId}/exists")
    @Operation(summary = "Check whether current user bookmarked a novel")
    public ResponseEntity<Map<String, Boolean>> existsMyNovelBookmark(@PathVariable Long novelId) {
        return ResponseEntity.ok(Map.of("bookmarked", bookmarkService.existsMyNovelBookmark(novelId)));
    }

    @GetMapping("/chapters/{chapterId}/exists")
    @Operation(summary = "Check whether current user bookmarked a chapter")
    public ResponseEntity<Map<String, Boolean>> existsMyChapterBookmark(@PathVariable Long chapterId) {
        return ResponseEntity.ok(Map.of("bookmarked", bookmarkService.existsMyChapterBookmark(chapterId)));
    }

    @PostMapping
    @Operation(summary = "Create a bookmark for a novel or chapter")
    public ResponseEntity<BookmarkDTO> createBookmark(@RequestBody BookmarkCreateDTO request) {
        return ResponseEntity.ok(bookmarkService.createBookmark(request));
    }

    @PostMapping("/novels/{novelId}")
    @Operation(summary = "Bookmark a novel")
    public ResponseEntity<BookmarkDTO> createNovelBookmark(@PathVariable Long novelId) {
        return ResponseEntity.ok(bookmarkService.createNovelBookmark(novelId));
    }

    @PostMapping("/chapters/{chapterId}")
    @Operation(summary = "Bookmark a chapter")
    public ResponseEntity<BookmarkDTO> createChapterBookmark(@PathVariable Long chapterId) {
        return ResponseEntity.ok(bookmarkService.createChapterBookmark(chapterId));
    }

    @DeleteMapping("/{bookmarkId}")
    @Operation(summary = "Delete current user's bookmark by id")
    public ResponseEntity<Void> deleteMyBookmark(@PathVariable Long bookmarkId) {
        bookmarkService.deleteMyBookmark(bookmarkId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/novels/{novelId}")
    @Operation(summary = "Delete current user's bookmark for a novel")
    public ResponseEntity<Void> deleteMyNovelBookmark(@PathVariable Long novelId) {
        bookmarkService.deleteMyNovelBookmark(novelId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/chapters/{chapterId}")
    @Operation(summary = "Delete current user's bookmark for a chapter")
    public ResponseEntity<Void> deleteMyChapterBookmark(@PathVariable Long chapterId) {
        bookmarkService.deleteMyChapterBookmark(chapterId);
        return ResponseEntity.noContent().build();
    }
}
