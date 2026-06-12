package com.example.netnovel_crawler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "crawl_chapter_records",
    uniqueConstraints = @UniqueConstraint(columnNames = {"source_name", "source_chapter_url"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlChapterRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false)
    private String sourceName;

    @Column(name = "source_chapter_url", nullable = false, length = 2048)
    private String sourceChapterUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrawlChapterStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime crawledAt;

    @PrePersist
    @PreUpdate
    private void setCrawledAt() {
        if (crawledAt == null) {
            crawledAt = LocalDateTime.now();
        }
    }
}
