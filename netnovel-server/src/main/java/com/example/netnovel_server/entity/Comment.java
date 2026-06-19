package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "comments",
    indexes = {
        @Index(name = "idx_comments_novel_parent_activity", columnList = "novel_id, parent_comment_id, last_activity_at"),
        @Index(name = "idx_comments_chapter_parent_activity", columnList = "chapter_id, parent_comment_id, last_activity_at"),
        @Index(name = "idx_comments_parent_created_at", columnList = "parent_comment_id, created_at"),
        @Index(name = "idx_comments_root_created_at", columnList = "root_comment_id, created_at"),
        @Index(name = "idx_comments_user_created_at", columnList = "user_id, created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Novel novel;

    // Null when the comment belongs directly to a novel.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Chapter chapter;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastActivityAt;

    // Null for root comments; set when this comment is a reply.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Comment parentComment;

    // Null for root comments; points to the top-level comment for replies.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_comment_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Comment rootComment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(nullable = false)
    @Builder.Default
    private Long replyCount = 0L;

    private LocalDateTime deletedAt;

    @PrePersist
    private void setCreateTime() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        lastActivityAt = now;
        if (deleted == null) {
            deleted = false;
        }
        if (replyCount == null) {
            replyCount = 0L;
        }
    }

    @PreUpdate
    private void setLastActivityAt() {
        lastActivityAt = LocalDateTime.now();
    }
}
