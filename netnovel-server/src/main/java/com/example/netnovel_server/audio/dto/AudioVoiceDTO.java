package com.example.netnovel_server.audio.dto;

import com.example.netnovel_server.audio.entity.AudioProvider;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioVoiceDTO {

    private Long id;
    private AudioProvider provider;
    private String languageCode;
    private String voiceName;
    private String displayName;
    private String gender;
    private String engine;
    private boolean enabled;
    private boolean defaultVoice;
    private Integer sortOrder;
}
