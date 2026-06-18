package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "crawl_tasks",
    indexes = {
        @Index(name = "idx_crawl_tasks_status_create_at", columnList = "status, create_at"),
        @Index(name = "idx_crawl_tasks_requested_by_create_at", columnList = "requested_by_user_id, create_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CrawlTaskStatus status = CrawlTaskStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User requestedBy;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

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
