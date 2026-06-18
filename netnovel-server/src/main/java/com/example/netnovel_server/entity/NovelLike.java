package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "novel_likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "novel_id"}),
    indexes = {
        @Index(name = "idx_novel_likes_novel", columnList = "novel_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Novel novel;

    // Time when the user liked the novel.
    @Column(nullable = false, updatable = false)
    private LocalDateTime likedAt;

    @PrePersist
    private void setLikedAt() {
        likedAt = LocalDateTime.now();
    }
}
