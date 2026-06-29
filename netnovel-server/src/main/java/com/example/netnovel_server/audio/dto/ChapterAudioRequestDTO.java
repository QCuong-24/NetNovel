package com.example.netnovel_server.audio.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterAudioRequestDTO {

    private String languageCode;

    private String voiceName;

    private String engine;

    private BigDecimal speakingRate;

    private BigDecimal pitch;

    private String audioEncoding;
}
