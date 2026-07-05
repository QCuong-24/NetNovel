package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.BookmarkDTO;
import com.example.netnovel_server.service.BookmarkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookmarkControllerTest {

    /*
     * Controller contract scope:
     * - Standalone MockMvc verifies bookmark route mapping, JSON body binding, and response status.
     * - BookmarkService is mocked; ownership/security/business rules are covered in BookmarkServiceTest.
     * - Paged bookmark list routes are left for fuller MVC/Jackson tests.
     */

    @Mock
    private BookmarkService bookmarkService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BookmarkController(bookmarkService)).build();
    }

    @Test
    void getMyBookmarkReturnsBookmarkDetail() throws Exception {
        when(bookmarkService.getMyBookmark(100L)).thenReturn(bookmark());

        mockMvc.perform(get("/api/bookmarks/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bookmarkId").value(100))
            .andExpect(jsonPath("$.novelTitle").value("Bookmarked Novel"));
    }

    @Test
    void existsMyNovelBookmarkReturnsBooleanPayload() throws Exception {
        when(bookmarkService.existsMyNovelBookmark(10L)).thenReturn(true);

        mockMvc.perform(get("/api/bookmarks/novels/10/exists"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bookmarked").value(true));
    }

    @Test
    void createBookmarkPostsGenericBookmarkPayload() throws Exception {
        when(bookmarkService.createBookmark(argThat(request ->
            request.getNovelId().equals(10L) && request.getChapterId() == null
        ))).thenReturn(bookmark());

        mockMvc.perform(post("/api/bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"novelId\":10}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.novelId").value(10));
    }

    @Test
    void createChapterBookmarkUsesPathChapterId() throws Exception {
        BookmarkDTO chapterBookmark = BookmarkDTO.builder()
            .bookmarkId(101L)
            .userId(7L)
            .novelId(10L)
            .chapterId(20L)
            .chapterTitle("Chapter One")
            .chapterNumber(1)
            .build();
        when(bookmarkService.createChapterBookmark(20L)).thenReturn(chapterBookmark);

        mockMvc.perform(post("/api/bookmarks/chapters/20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chapterId").value(20))
            .andExpect(jsonPath("$.chapterTitle").value("Chapter One"));
    }

    @Test
    void deleteMyNovelBookmarkReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/bookmarks/novels/10"))
            .andExpect(status().isNoContent());

        verify(bookmarkService).deleteMyNovelBookmark(10L);
    }

    private static BookmarkDTO bookmark() {
        return BookmarkDTO.builder()
            .bookmarkId(100L)
            .userId(7L)
            .novelId(10L)
            .novelTitle("Bookmarked Novel")
            .author("Author")
            .build();
    }
}
