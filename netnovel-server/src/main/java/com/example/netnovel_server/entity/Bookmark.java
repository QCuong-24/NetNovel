package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "bookmarks",
    indexes = {
        @Index(name = "idx_bookmarks_novel", columnList = "novel_id"),
        @Index(name = "idx_bookmarks_chapter", columnList = "chapter_id"),
        @Index(name = "idx_bookmarks_user_created_at", columnList = "user_id, created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Chapter chapter;

    // Time when the user bookmarked the novel or chapter.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void setCreatedAt() {
        createdAt = LocalDateTime.now();
    }
}
