package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "bookmarks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "chapter_id"})
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
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    // Time when the user bookmarked the chapter.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void setCreatedAt() {
        createdAt = LocalDateTime.now();
    }
}
