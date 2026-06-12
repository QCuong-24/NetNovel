package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "novel_views",
    indexes = {
        @Index(name = "idx_novel_views_novel", columnList = "novel_id"),
        @Index(name = "idx_novel_views_user", columnList = "user_id"),
        @Index(name = "idx_novel_views_viewed_at", columnList = "viewed_at"),
        @Index(name = "idx_novel_views_novel_viewed_at", columnList = "novel_id, viewed_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Novel novel;

    // Null when the view comes from an anonymous reader.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime viewedAt;

    @PrePersist
    private void setViewedAt() {
        viewedAt = LocalDateTime.now();
    }
}
