package com.example.netnovel_server.audio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "audio_voices",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_audio_voice_provider_voice_engine",
        columnNames = {"provider", "voice_name", "engine"}
    ),
    indexes = {
        @Index(name = "idx_audio_voice_language", columnList = "language_code"),
        @Index(name = "idx_audio_voice_enabled", columnList = "enabled")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioVoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AudioProvider provider;

    @Column(nullable = false, length = 20)
    private String languageCode;

    @Column(nullable = false, length = 100)
    private String voiceName;

    @Column(nullable = false, length = 120)
    private String displayName;

    @Column(length = 30)
    private String gender;

    @Column(nullable = false, length = 50)
    private String engine;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean defaultVoice = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void setCreateTime() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void setUpdateTime() {
        updatedAt = LocalDateTime.now();
    }
}
