package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.BookmarkDTO;
import com.example.netnovel_server.dto.CommentDTO;
import com.example.netnovel_server.security.CustomUserDetailsService;
import com.example.netnovel_server.service.BookmarkService;
import com.example.netnovel_server.service.CommentService;
import com.example.netnovel_server.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CommentController.class, BookmarkController.class})
@AutoConfigureMockMvc(addFilters = false)
class CommentBookmarkWebMvcTest {

    /*
     * MVC slice scope:
     * - Loads Spring MVC/Jackson infrastructure, not the full application context or database.
     * - Security filters are disabled here so these tests focus on endpoint mapping, Pageable binding,
     *   and Page<T> JSON serialization.
     * - Service business rules remain in service tests; non-paged route contracts remain in standalone controller tests.
     */

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private BookmarkService bookmarkService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // Test cases for pageable binding and page serialization
    @Test
    void getNovelCommentsBindsPageableAndSerializesPage() throws Exception {
        when(commentService.getNovelComments(argThat(id -> id == 10L), argThat(pageable ->
            pageable.getPageNumber() == 1 && pageable.getPageSize() == 5
        ))).thenReturn(new PageImpl<>(
            List.of(comment(100L, "Novel comment")),
            PageRequest.of(1, 5),
            6
        ));

        mockMvc.perform(get("/api/novels/10/comments?page=1&size=5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].commentId").value(100))
            .andExpect(jsonPath("$.content[0].content").value("Novel comment"))
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.size").value(5))
            .andExpect(jsonPath("$.totalElements").value(6));
    }

    // Additional test cases for chapter comments and bookmarks
    @Test
    void getChapterCommentsBindsPageableAndSerializesPage() throws Exception {
        when(commentService.getChapterComments(argThat(id -> id == 20L), argThat(pageable ->
            pageable.getPageNumber() == 0 && pageable.getPageSize() == 3
        ))).thenReturn(new PageImpl<>(
            List.of(comment(101L, "Chapter comment")),
            PageRequest.of(0, 3),
            1
        ));

        mockMvc.perform(get("/api/chapters/20/comments?page=0&size=3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].commentId").value(101))
            .andExpect(jsonPath("$.content[0].content").value("Chapter comment"))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    // Test cases for bookmarks
    @Test
    void getMyBookmarksBindsPageableAndSerializesPage() throws Exception {
        when(bookmarkService.getMyBookmarks(argThat(pageable ->
            pageable.getPageNumber() == 2 && pageable.getPageSize() == 4
        ))).thenReturn(new PageImpl<>(
            List.of(bookmark(100L, null)),
            PageRequest.of(2, 4),
            9
        ));

        mockMvc.perform(get("/api/bookmarks?page=2&size=4"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].bookmarkId").value(100))
            .andExpect(jsonPath("$.content[0].novelTitle").value("Bookmarked Novel"))
            .andExpect(jsonPath("$.number").value(2))
            .andExpect(jsonPath("$.totalElements").value(9));
    }

    // Test case for chapter bookmarks
    @Test
    void getMyChapterBookmarksBindsPageableAndSerializesPage() throws Exception {
        when(bookmarkService.getMyChapterBookmarks(argThat(pageable ->
            pageable.getPageNumber() == 0 && pageable.getPageSize() == 2
        ))).thenReturn(new PageImpl<>(
            List.of(bookmark(101L, 20L)),
            PageRequest.of(0, 2),
            1
        ));

        mockMvc.perform(get("/api/bookmarks/chapters?page=0&size=2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].chapterId").value(20))
            .andExpect(jsonPath("$.content[0].chapterTitle").value("Chapter One"))
            .andExpect(jsonPath("$.size").value(2));
    }

    private static CommentDTO comment(Long id, String content) {
        return CommentDTO.builder()
            .commentId(id)
            .novelId(10L)
            .userId(7L)
            .username("reader")
            .content(content)
            .deleted(false)
            .replyCount(0L)
            .build();
    }

    private static BookmarkDTO bookmark(Long id, Long chapterId) {
        return BookmarkDTO.builder()
            .bookmarkId(id)
            .userId(7L)
            .novelId(10L)
            .novelTitle("Bookmarked Novel")
            .author("Author")
            .chapterId(chapterId)
            .chapterTitle(chapterId == null ? null : "Chapter One")
            .chapterNumber(chapterId == null ? null : 1)
            .build();
    }
}
