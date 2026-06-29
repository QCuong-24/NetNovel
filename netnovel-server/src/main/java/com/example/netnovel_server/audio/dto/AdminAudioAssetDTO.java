package com.example.netnovel_server.audio.dto;

import com.example.netnovel_server.audio.entity.AudioProvider;
import com.example.netnovel_server.audio.entity.ChapterAudioStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAudioAssetDTO {

    private Long assetId;
    private Long chapterId;
    private Long novelId;
    private String chapterTitle;
    private String novelTitle;
    private ChapterAudioStatus status;
    private AudioProvider provider;
    private String languageCode;
    private String voiceName;
    private String engine;
    private Long requestedByUserId;
    private Integer sourceTextCharacters;
    private Integer chunkCount;
    private Integer providerRequestCount;
    private Integer providerCharacterCount;
    private Long fileSizeBytes;
    private Long generationDurationMs;
    private Long cacheHitCount;
    private Integer retryCount;
    private String lastErrorCode;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
