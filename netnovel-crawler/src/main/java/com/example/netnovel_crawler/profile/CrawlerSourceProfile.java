package com.example.netnovel_crawler.profile;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(
    name = "crawler_source_profiles",
    uniqueConstraints = @UniqueConstraint(columnNames = {"domain", "version"}),
    indexes = {
        @Index(name = "idx_crawler_source_profiles_domain_enabled", columnList = "domain, enabled"),
        @Index(name = "idx_crawler_source_profiles_validation_status", columnList = "validation_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlerSourceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String domain;

    @Column(nullable = false)
    private String sourceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private CrawlerEngine engine = CrawlerEngine.JSOUP;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private SourceProfileCreatedBy createdBy = SourceProfileCreatedBy.MANUAL;

    private Double confidence;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(nullable = false, length = 2048)
    private String sampleNovelUrl;

    @Column(length = 2048)
    private String novelUrlPattern;

    @Column(length = 2048)
    private String titleSelector;

    @Column(length = 2048)
    private String authorSelector;

    @Column(length = 2048)
    private String descriptionSelector;

    @Column(length = 2048)
    private String coverImageSelector;

    @Column(length = 2048)
    private String genreSelector;

    @Column(length = 2048)
    private String tagSelector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ChapterDiscoveryType chapterDiscoveryType = ChapterDiscoveryType.LIST;

    @Column(length = 2048)
    private String chapterListSelector;

    @Column(length = 2048)
    private String chapterUrlSelector;

    @Column(length = 2048)
    private String totalChapterSelector;

    @Column(length = 2048)
    private String chapterUrlPattern;

    @Column(length = 2048)
    private String chapterTitleSelector;

    @Column(length = 2048)
    private String chapterContentSelector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private SourceProfileValidationStatus validationStatus = SourceProfileValidationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String validationError;

    private LocalDateTime lastValidatedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(nullable = false)
    private LocalDateTime updateAt;

    @PrePersist
    private void setCreateTime() {
        LocalDateTime now = LocalDateTime.now();
        createAt = now;
        updateAt = now;
        applyDefaults();
    }

    @PreUpdate
    private void setUpdateTime() {
        updateAt = LocalDateTime.now();
        applyDefaults();
    }

    private void applyDefaults() {
        domain = normalizeDomain(domain);
        if (engine == null) {
            engine = CrawlerEngine.JSOUP;
        }
        if (enabled == null) {
            enabled = true;
        }
        if (createdBy == null) {
            createdBy = SourceProfileCreatedBy.MANUAL;
        }
        if (version == null) {
            version = 1;
        }
        if (chapterDiscoveryType == null) {
            chapterDiscoveryType = ChapterDiscoveryType.LIST;
        }
        if (validationStatus == null) {
            validationStatus = SourceProfileValidationStatus.PENDING;
        }
    }

    private String normalizeDomain(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("www.") ? normalized.substring(4) : normalized;
    }
}
