package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.CommentDTO;
import com.example.netnovel_server.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    /*
     * Controller contract scope:
     * - Standalone MockMvc verifies non-paged comment routes and request/response JSON.
     * - CommentService is mocked; ownership, notifications, and soft-delete rules live in CommentServiceTest.
     * - Paged root-comment routes are left for fuller MVC/Jackson tests.
     */

    @Mock
    private CommentService commentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CommentController(commentService)).build();
    }

    @Test
    void getRepliesReturnsReplyList() throws Exception {
        when(commentService.getReplies(100L)).thenReturn(List.of(comment(101L, "reply")));

        mockMvc.perform(get("/api/comments/100/replies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].commentId").value(101))
            .andExpect(jsonPath("$[0].content").value("reply"));
    }

    @Test
    void getCommentContextReturnsParentChain() throws Exception {
        when(commentService.getCommentContext(101L)).thenReturn(List.of(comment(101L, "reply"), comment(100L, "root")));

        mockMvc.perform(get("/api/comments/101/context"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].content").value("reply"))
            .andExpect(jsonPath("$[1].content").value("root"));
    }

    @Test
    void createNovelCommentPostsContentToService() throws Exception {
        when(commentService.createNovelComment(argThat(id -> id == 10L), argThat(request ->
            "Nice novel".equals(request.getContent())
        ))).thenReturn(comment(100L, "Nice novel"));

        mockMvc.perform(post("/api/novels/10/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Nice novel\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Nice novel"));
    }

    @Test
    void createReplyPostsContentToParentComment() throws Exception {
        when(commentService.createReply(argThat(id -> id == 100L), argThat(request ->
            "Thanks".equals(request.getContent())
        ))).thenReturn(comment(101L, "Thanks"));

        mockMvc.perform(post("/api/comments/100/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Thanks\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentId").value(101));
    }

    @Test
    void updateCommentPutsContentToService() throws Exception {
        when(commentService.updateComment(argThat(id -> id == 100L), argThat(request ->
            "Updated".equals(request.getContent())
        ))).thenReturn(comment(100L, "Updated"));

        mockMvc.perform(put("/api/comments/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Updated\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Updated"));
    }

    @Test
    void deleteCommentReturnsSoftDeletedComment() throws Exception {
        when(commentService.deleteComment(100L)).thenReturn(CommentDTO.builder()
            .commentId(100L)
            .userId(7L)
            .username("reader")
            .content("This comment was deleted")
            .deleted(true)
            .replyCount(0L)
            .build());

        mockMvc.perform(delete("/api/comments/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deleted").value(true))
            .andExpect(jsonPath("$.content").value("This comment was deleted"));
    }

    @Test
    void moderateDeleteCommentUsesModerationRoute() throws Exception {
        when(commentService.moderateDeleteComment(100L)).thenReturn(comment(100L, "moderated"));

        mockMvc.perform(delete("/api/comments/100/moderation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentId").value(100));
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
}
