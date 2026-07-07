package com.example.netnovel_server.audio.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioVoiceUpdateDTO {

    private Boolean enabled;
    private Boolean defaultVoice;
    @Min(value = 0, message = "Sort order must be at least 0")
    private Integer sortOrder;
}
