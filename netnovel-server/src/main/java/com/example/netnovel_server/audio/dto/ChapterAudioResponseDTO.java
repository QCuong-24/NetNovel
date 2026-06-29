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
public class ChapterAudioResponseDTO {

    private Long assetId;

    private Long chapterId;

    private ChapterAudioStatus status;

    private String audioUrl;

    private boolean cached;

    private AudioProvider provider;

    private String languageCode;

    private String voiceName;

    private String engine;

    private String audioEncoding;

    private Long fileSizeBytes;

    private Long durationMs;

    private String errorMessage;

    private LocalDateTime expiresAt;
}
