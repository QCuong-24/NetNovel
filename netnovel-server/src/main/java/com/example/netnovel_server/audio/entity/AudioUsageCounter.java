package com.example.netnovel_server.audio.entity;

import com.example.netnovel_server.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "audio_usage_counters",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_audio_usage_user_period",
        columnNames = {"user_id", "period_type", "period_start"}
    ),
    indexes = {
        @Index(name = "idx_audio_usage_period", columnList = "period_type, period_start"),
        @Index(name = "idx_audio_usage_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioUsageCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AudioUsagePeriodType periodType;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Builder.Default
    @Column(nullable = false)
    private Long requestedCharacters = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long generatedCharacters = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long generationCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long cacheHitCount = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void setCreateTime() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        normalizeCounters();
    }

    @PreUpdate
    private void setUpdateTime() {
        updatedAt = LocalDateTime.now();
        normalizeCounters();
    }

    private void normalizeCounters() {
        if (requestedCharacters == null) {
            requestedCharacters = 0L;
        }
        if (generatedCharacters == null) {
            generatedCharacters = 0L;
        }
        if (generationCount == null) {
            generationCount = 0L;
        }
        if (cacheHitCount == null) {
            cacheHitCount = 0L;
        }
    }
}
