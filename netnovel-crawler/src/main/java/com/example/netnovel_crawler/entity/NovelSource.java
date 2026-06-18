package com.example.netnovel_crawler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "novel_sources",
    uniqueConstraints = @UniqueConstraint(columnNames = {"source_name", "source_novel_url"}),
    indexes = {
        @Index(name = "idx_novel_sources_novel", columnList = "novel_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Novel novel;

    @Column(name = "source_name", nullable = false)
    private String sourceName;

    @Column(name = "source_novel_url", nullable = false, length = 2048)
    private String sourceNovelUrl;

    private String externalId;

    private LocalDateTime lastCrawledAt;
}
