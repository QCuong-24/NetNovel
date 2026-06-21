package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.CommentCreateDTO;
import com.example.netnovel_server.dto.CommentDTO;
import com.example.netnovel_server.entity.*;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.ForbiddenException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.CommentMapper;
import com.example.netnovel_server.repository.*;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private static final String DELETED_COMMENT_CONTENT = "This comment was deleted";

    private final CommentRepository commentRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserEventService userEventService;

    public CommentService(
        CommentRepository commentRepository,
        NovelRepository novelRepository,
        ChapterRepository chapterRepository,
        UserRepository userRepository,
        NotificationService notificationService,
        UserEventService userEventService
    ) {
        this.commentRepository = commentRepository;
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.userEventService = userEventService;
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> getNovelComments(Long novelId, Pageable pageable) {
        ensureNovelExists(novelId);
        return commentRepository.findByNovelIdAndParentCommentIsNullOrderByLastActivityAtDesc(novelId, pageable)
            .map(CommentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> getChapterComments(Long chapterId, Pageable pageable) {
        ensureChapterExists(chapterId);
        return commentRepository.findByChapterIdAndParentCommentIsNullOrderByLastActivityAtDesc(chapterId, pageable)
            .map(CommentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getReplies(Long commentId) {
        ensureCommentExists(commentId);
        return commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId).stream()
            .map(CommentMapper::toDTO)
            .toList();
    }

    @Transactional
    public CommentDTO createNovelComment(Long novelId, CommentCreateDTO request) {
        User user = getCurrentUser();
        Novel novel = novelRepository.findById(novelId)
            .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));

        Comment comment = CommentMapper.toEntity(validateContent(request), user, novel, null, null, null);
        Comment savedComment = commentRepository.save(comment);
        userEventService.recordForCurrentUser(UserEventType.CREATE_COMMENT, novel, null);
        return CommentMapper.toDTO(savedComment);
    }

    @Transactional
    public CommentDTO createChapterComment(Long chapterId, CommentCreateDTO request) {
        User user = getCurrentUser();
        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        Comment comment = CommentMapper.toEntity(
            validateContent(request),
            user,
            chapter.getNovel(),
            chapter,
            null,
            null
        );
        Comment savedComment = commentRepository.save(comment);
        userEventService.recordForCurrentUser(UserEventType.CREATE_COMMENT, chapter.getNovel(), chapter);
        return CommentMapper.toDTO(savedComment);
    }

    @Transactional
    public CommentDTO createReply(Long parentCommentId, CommentCreateDTO request) {
        User user = getCurrentUser();
        Comment parent = findComment(parentCommentId);
        Comment root = parent.getRootComment() != null ? parent.getRootComment() : parent;

        Comment reply = CommentMapper.toEntity(
            validateContent(request),
            user,
            parent.getNovel(),
            parent.getChapter(),
            parent,
            root
        );

        root.setLastActivityAt(LocalDateTime.now());
        commentRepository.save(root);

        parent.setReplyCount(safeIncrement(parent.getReplyCount()));
        commentRepository.save(parent);

        Comment savedReply = commentRepository.save(reply);
        sendReplyNotification(parent, savedReply);
        userEventService.recordForCurrentUser(UserEventType.REPLY_COMMENT, savedReply.getNovel(), savedReply.getChapter());
        return CommentMapper.toDTO(savedReply);
    }

    @Transactional
    public CommentDTO updateComment(Long commentId, CommentCreateDTO request) {
        Comment comment = findComment(commentId);
        ensureOwner(comment);

        if (Boolean.TRUE.equals(comment.getDeleted())) {
            throw new BadRequestException("Deleted comment cannot be updated");
        }

        comment.setContent(validateContent(request));
        touchRootIfReply(comment);

        return CommentMapper.toDTO(commentRepository.save(comment));
    }

    @Transactional
    public CommentDTO deleteComment(Long commentId) {
        Comment comment = findComment(commentId);
        ensureOwner(comment);

        return softDelete(comment);
    }

    @Transactional
    public CommentDTO moderateDeleteComment(Long commentId) {
        Comment comment = findComment(commentId);
        return softDelete(comment);
    }

    private CommentDTO softDelete(Comment comment) {
        comment.setContent(DELETED_COMMENT_CONTENT);
        comment.setDeleted(true);
        comment.setDeletedAt(LocalDateTime.now());
        touchRootIfReply(comment);

        return CommentMapper.toDTO(commentRepository.save(comment));
    }

    private String validateContent(CommentCreateDTO request) {
        if (request == null || request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("Comment content is required");
        }
        return request.getContent().trim();
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
    }

    private void ensureCommentExists(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found");
        }
    }

    private void ensureNovelExists(Long novelId) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel not found");
        }
    }

    private void ensureChapterExists(Long chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter not found");
        }
    }

    private User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void ensureOwner(Comment comment) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this comment");
        }
    }

    private void touchRootIfReply(Comment comment) {
        Comment root = comment.getRootComment();
        if (root != null) {
            root.setLastActivityAt(LocalDateTime.now());
            commentRepository.save(root);
        }
    }

    private Long safeIncrement(Long value) {
        return value == null ? 1L : value + 1;
    }

    private void sendReplyNotification(Comment parent, Comment reply) {
        User recipient = parent.getUser();
        if (recipient.getId().equals(reply.getUser().getId())) {
            return;
        }

        notificationService.createNotification(
            recipient,
            NotificationService.TYPE_COMMENT_REPLY,
            "New reply to your comment",
            reply.getUser().getUsername() + " replied to your comment.",
            "/comments/" + parent.getId() + "/replies"
        );
    }
}
