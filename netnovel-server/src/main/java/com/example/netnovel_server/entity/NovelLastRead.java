package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "novel_last_reads",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "novel_id"}),
    indexes = {
        @Index(name = "idx_novel_last_reads_chapter", columnList = "chapter_id"),
        @Index(name = "idx_novel_last_reads_user_last_read_at", columnList = "user_id, last_read_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelLastRead {

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Chapter chapter;

    // Time when the user last read this novel.
    @Column(nullable = false)
    private LocalDateTime lastReadAt;

    @PrePersist
    @PreUpdate
    private void setLastReadAt() {
        lastReadAt = LocalDateTime.now();
    }
}
