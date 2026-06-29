package com.example.netnovel_server.audio.entity;

import com.example.netnovel_server.entity.Chapter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "chapter_audio_assets",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_chapter_audio_cache_key",
        columnNames = {
            "chapter_id",
            "provider",
            "language_code",
            "voice_name",
            "engine",
            "speaking_rate",
            "pitch",
            "audio_encoding",
            "content_hash"
        }
    ),
    indexes = {
        @Index(name = "idx_chapter_audio_chapter", columnList = "chapter_id"),
        @Index(name = "idx_chapter_audio_status", columnList = "status"),
        @Index(name = "idx_chapter_audio_last_accessed", columnList = "last_accessed_at"),
        @Index(name = "idx_chapter_audio_expires", columnList = "expires_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterAudioAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Chapter chapter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AudioProvider provider;

    @Column(nullable = false, length = 20)
    private String languageCode;

    @Column(nullable = false, length = 100)
    private String voiceName;

    @Column(nullable = false, length = 50)
    private String engine;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal speakingRate;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal pitch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AudioEncoding audioEncoding;

    @Column(nullable = false, length = 64)
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChapterAudioStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AudioStorageType storageType;

    @Column(length = 500)
    private String storageKey;

    @Column(length = 1000)
    private String audioUrl;

    @Column(length = 100)
    private String mimeType;

    private Long fileSizeBytes;

    private Long durationMs;

    private Long requestedByUserId;

    private Integer sourceTextCharacters;

    private Integer chunkCount;

    private Long generationDurationMs;

    private Integer providerRequestCount;

    private Integer providerCharacterCount;

    @Builder.Default
    private Long cacheHitCount = 0L;

    @Builder.Default
    private Integer retryCount = 0;

    @Column(length = 100)
    private String lastErrorCode;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime lastAccessedAt;

    private LocalDateTime lastPlayedAt;

    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void setCreateTime() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (cacheHitCount == null) {
            cacheHitCount = 0L;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    @PreUpdate
    private void setUpdateTime() {
        updatedAt = LocalDateTime.now();
        if (cacheHitCount == null) {
            cacheHitCount = 0L;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
}
