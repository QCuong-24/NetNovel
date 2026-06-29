package com.example.netnovel_server.audio.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioVoiceUpdateDTO {

    private Boolean enabled;
    private Boolean defaultVoice;
    private Integer sortOrder;
}
