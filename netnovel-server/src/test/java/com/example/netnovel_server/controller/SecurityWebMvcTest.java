package com.example.netnovel_server.controller;

import com.example.netnovel_server.config.SecurityConfig;
import com.example.netnovel_server.dto.CommentDTO;
import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.security.CustomUserDetailsService;
import com.example.netnovel_server.service.CommentService;
import com.example.netnovel_server.service.JwtService;
import com.example.netnovel_server.service.NovelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {NovelController.class, CommentController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class SecurityWebMvcTest {

    /*
     * Security MVC slice scope:
     * - Loads the real SecurityConfig and Spring Security filter chain.
     * - Uses @WithMockUser for authenticated/role-based requests instead of JWT parsing.
     * - JwtService and CustomUserDetailsService are mocked only so JwtAuthenticationFilter can be constructed.
     * - Controller services are mocked so these tests check security decisions, not business rules or persistence.
     */

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NovelService novelService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void publicNovelGetDoesNotRequireAuthentication() throws Exception {
        when(novelService.getNovel(10L)).thenReturn(novel());

        mockMvc.perform(get("/api/novels/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.novelId").value(10))
            .andExpect(jsonPath("$.title").value("NetNovel Sample"));
    }

    @Test
    void createNovelRejectsAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/novels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(novelRequestJson()))
            .andExpect(status().isForbidden());

        verify(novelService, never()).createNovel(any());
    }

    @Test
    @WithMockUser
    void createNovelAllowsAuthenticatedUser() throws Exception {
        when(novelService.createNovel(argThat(request ->
            "NetNovel Sample".equals(request.getTitle())
                && request.getGenres().contains("Fantasy")
        ))).thenReturn(novel());

        mockMvc.perform(post("/api/novels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(novelRequestJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("NetNovel Sample"));
    }

    @Test
    void publicCommentRepliesDoNotRequireAuthentication() throws Exception {
        when(commentService.getReplies(100L)).thenReturn(java.util.List.of(comment()));

        mockMvc.perform(get("/api/comments/100/replies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].commentId").value(101));
    }

    @Test
    @WithMockUser(roles = "USER")
    void moderationDeleteRejectsRegularUser() throws Exception {
        mockMvc.perform(delete("/api/comments/100/moderation"))
            .andExpect(status().isForbidden());

        verify(commentService, never()).moderateDeleteComment(100L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void moderationDeleteAllowsManager() throws Exception {
        when(commentService.moderateDeleteComment(100L)).thenReturn(comment());

        mockMvc.perform(delete("/api/comments/100/moderation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentId").value(101))
            .andExpect(jsonPath("$.content").value("moderated"));
    }

    private static String novelRequestJson() {
        return """
            {
              "title": "NetNovel Sample",
              "author": "Author",
              "description": "Description",
              "genres": ["Fantasy"],
              "tags": ["Cultivation"],
              "status": "ONGOING",
              "accessStatus": "NORMAL"
            }
            """;
    }

    private static NovelDTO novel() {
        return NovelDTO.builder()
            .novelId(10L)
            .title("NetNovel Sample")
            .author("Author")
            .description("Description")
            .genres(Set.of("Fantasy"))
            .status("ONGOING")
            .accessStatus("NORMAL")
            .chapterCount(0)
            .build();
    }

    private static CommentDTO comment() {
        return CommentDTO.builder()
            .commentId(101L)
            .novelId(10L)
            .userId(7L)
            .username("reader")
            .content("moderated")
            .deleted(false)
            .replyCount(0L)
            .build();
    }
}
