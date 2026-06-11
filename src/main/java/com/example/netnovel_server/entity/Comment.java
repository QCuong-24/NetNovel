package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
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

    private LocalDateTime deletedAt;

    @PrePersist
    private void setCreateTime() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        lastActivityAt = now;
        if (deleted == null) {
            deleted = false;
        }
    }

    @PreUpdate
    private void setLastActivityAt() {
        lastActivityAt = LocalDateTime.now();
    }
}
