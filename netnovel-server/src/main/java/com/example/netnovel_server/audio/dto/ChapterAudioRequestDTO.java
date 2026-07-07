package com.example.netnovel_server.audio.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterAudioRequestDTO {

    @Size(max = 32, message = "Language code must not exceed 32 characters")
    private String languageCode;

    @Size(max = 100, message = "Voice name must not exceed 100 characters")
    private String voiceName;

    @Size(max = 32, message = "Engine must not exceed 32 characters")
    private String engine;

    @DecimalMin(value = "0.50", message = "Speaking rate must be at least 0.50")
    @DecimalMax(value = "2.00", message = "Speaking rate must not exceed 2.00")
    private BigDecimal speakingRate;

    @DecimalMin(value = "-20.00", message = "Pitch must be at least -20.00")
    @DecimalMax(value = "20.00", message = "Pitch must not exceed 20.00")
    private BigDecimal pitch;

    @Size(max = 32, message = "Audio encoding must not exceed 32 characters")
    private String audioEncoding;
}
