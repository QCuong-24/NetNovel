package com.example.netnovel_crawler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawl_tasks")
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
    private CrawlTaskStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(nullable = false)
    private LocalDateTime updateAt;

    @PreUpdate
    private void setUpdateTime() {
        updateAt = LocalDateTime.now();
    }
}
