package com.example.netnovel_crawler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "novels",
    indexes = {
        @Index(name = "idx_novels_status", columnList = "status"),
        @Index(name = "idx_novels_update_at", columnList = "update_at"),
        @Index(name = "idx_novels_views", columnList = "views"),
        @Index(name = "idx_novels_follows", columnList = "follows"),
        @Index(name = "idx_novels_likes", columnList = "likes")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Novel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverImageUrl;

    private String coverImagePublicId;

    @Column(nullable = false)
    @Builder.Default
    private Long views = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long follows = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long likes = 0L;

    @ManyToMany
    @JoinTable(
        name = "novel_tags",
        joinColumns = @JoinColumn(name = "novel_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"),
        indexes = {
            @Index(name = "idx_novel_tags_novel", columnList = "novel_id"),
            @Index(name = "idx_novel_tags_tag", columnList = "tag_id")
        }
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(nullable = false)
    private LocalDateTime updateAt;

    @PrePersist
    private void setCreateTime() {
        LocalDateTime now = LocalDateTime.now();
        createAt = now;
        updateAt = now;
    }

    @PreUpdate
    private void setUpdateTime() {
        updateAt = LocalDateTime.now();
    }
}
