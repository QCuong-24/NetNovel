package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.CommentCreateDTO;
import com.example.netnovel_server.dto.CommentDTO;
import com.example.netnovel_server.entity.AuthProvider;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Comment;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Role;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.entity.UserEventType;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.ForbiddenException;
import com.example.netnovel_server.repository.ChapterRepository;
import com.example.netnovel_server.repository.CommentRepository;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    /*
     * Unit scope:
     * - CommentService create, reply, update, and soft-delete behavior.
     * - SecurityUtils is statically mocked so ownership checks are deterministic.
     * - Repository saves are captured to verify relationships and trimmed content.
     */

    private static final Long CURRENT_USER_ID = 7L;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NovelRepository novelRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserEventService userEventService;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(
            commentRepository,
            novelRepository,
            chapterRepository,
            userRepository,
            notificationService,
            userEventService
        );
    }

    // Create flow: creating a novel comment trims content and records a user event.
    @Test
    void createNovelCommentTrimsContentAndRecordsUserEvent() {
        User user = user(CURRENT_USER_ID, "reader");
        Novel novel = novel(10L);
        CommentCreateDTO request = new CommentCreateDTO("  thoughtful comment  ");

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(user));
        when(novelRepository.findById(novel.getId())).thenReturn(Optional.of(novel));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(100L);
            return comment;
        });

        try (MockedStatic<SecurityUtils> security = currentUser()) {
            CommentDTO response = commentService.createNovelComment(novel.getId(), request);

            assertEquals(100L, response.getCommentId());
            assertEquals("thoughtful comment", response.getContent());
            assertEquals(novel.getId(), response.getNovelId());
        }

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        assertEquals(user, commentCaptor.getValue().getUser());
        assertEquals(novel, commentCaptor.getValue().getNovel());
        assertEquals("thoughtful comment", commentCaptor.getValue().getContent());
        verify(userEventService).recordForCurrentUser(UserEventType.CREATE_COMMENT, novel, null);
    }

    // Create flow: creating a chapter comment links the comment to both the chapter and its parent novel, and records a user event.
    @Test
    void createChapterCommentLinksCommentToChapterAndNovel() {
        User user = user(CURRENT_USER_ID, "reader");
        Novel novel = novel(10L);
        Chapter chapter = chapter(20L, novel);

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(user));
        when(chapterRepository.findById(chapter.getId())).thenReturn(Optional.of(chapter));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(101L);
            return comment;
        });

        try (MockedStatic<SecurityUtils> security = currentUser()) {
            CommentDTO response = commentService.createChapterComment(chapter.getId(), new CommentCreateDTO("chapter comment"));

            assertEquals(chapter.getId(), response.getChapterId());
            assertEquals(novel.getId(), response.getNovelId());
        }

        verify(userEventService).recordForCurrentUser(UserEventType.CREATE_COMMENT, novel, chapter);
    }

    // Create flow: creating a reply increments the parent comment's reply count and notifies the parent comment's owner.
    @Test
    void createReplyIncrementsParentAndNotifiesParentOwner() {
        User replier = user(CURRENT_USER_ID, "replier");
        User parentOwner = user(8L, "parentOwner");
        Novel novel = novel(10L);
        Comment parent = comment(100L, parentOwner, novel, null, "parent comment");
        parent.setReplyCount(2L);

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(replier));
        when(commentRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            if (comment.getId() == null) {
                comment.setId(200L);
            }
            return comment;
        });

        try (MockedStatic<SecurityUtils> security = currentUser()) {
            CommentDTO response = commentService.createReply(parent.getId(), new CommentCreateDTO("reply content"));

            assertEquals(200L, response.getCommentId());
            assertEquals("reply content", response.getContent());
        }

        assertEquals(3L, parent.getReplyCount());
        verify(notificationService).createNotification(
            parentOwner,
            NotificationService.TYPE_COMMENT_REPLY,
            "New reply to your comment in novel \"Commented Novel\"",
            "replier replied: \"reply content\"",
            "/novels/10#comment-200"
        );
        verify(userEventService).recordForCurrentUser(UserEventType.REPLY_COMMENT, novel, null);
    }

    // Update guards: updating a comment by a non-owner is rejected before any mutation or saving.
    @Test
    void updateCommentRejectsNonOwner() {
        Comment comment = comment(100L, user(8L, "other"), novel(10L), null, "original");

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        try (MockedStatic<SecurityUtils> security = currentUser()) {
            assertThrows(ForbiddenException.class, () -> commentService.updateComment(comment.getId(), new CommentCreateDTO("updated")));
        }

        verify(commentRepository, never()).save(any());
    }

    // Update guards: updating a comment with blank content is rejected before any mutation or saving.
    @Test
    void updateCommentRejectsBlankContent() {
        Comment comment = comment(100L, user(CURRENT_USER_ID, "reader"), novel(10L), null, "original");

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        try (MockedStatic<SecurityUtils> security = currentUser()) {
            assertThrows(BadRequestException.class, () -> commentService.updateComment(comment.getId(), new CommentCreateDTO("  ")));
        }

        verify(commentRepository, never()).save(any());
    }

    // Delete flow: deleting a comment by its owner soft-deletes the comment and updates its content to indicate deletion.
    @Test
    void deleteCommentSoftDeletesOwnedComment() {
        Comment comment = comment(100L, user(CURRENT_USER_ID, "reader"), novel(10L), null, "original");

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        try (MockedStatic<SecurityUtils> security = currentUser()) {
            CommentDTO response = commentService.deleteComment(comment.getId());

            assertTrue(response.getDeleted());
            assertEquals("This comment was deleted", response.getContent());
        }

        assertTrue(comment.getDeleted());
        assertEquals("This comment was deleted", comment.getContent());
    }

    private static MockedStatic<SecurityUtils> currentUser() {
        MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class);
        security.when(SecurityUtils::getCurrentUserIdOrThrow).thenReturn(CURRENT_USER_ID);
        return security;
    }

    private static User user(Long id, String username) {
        return User.builder()
            .id(id)
            .username(username)
            .email(username + "@example.com")
            .provider(AuthProvider.LOCAL)
            .roles(Set.of(Role.USER))
            .build();
    }

    private static Novel novel(Long id) {
        return Novel.builder()
            .id(id)
            .title("Commented Novel")
            .author("Author")
            .build();
    }

    private static Chapter chapter(Long id, Novel novel) {
        return Chapter.builder()
            .id(id)
            .novel(novel)
            .title("Chapter One")
            .chapterNumber(1)
            .build();
    }

    private static Comment comment(Long id, User user, Novel novel, Chapter chapter, String content) {
        return Comment.builder()
            .id(id)
            .user(user)
            .novel(novel)
            .chapter(chapter)
            .content(content)
            .deleted(false)
            .replyCount(0L)
            .build();
    }
}
