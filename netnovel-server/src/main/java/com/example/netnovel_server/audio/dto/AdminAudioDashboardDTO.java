package com.example.netnovel_server.audio.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAudioDashboardDTO {

    private long totalAssets;
    private long readyAssets;
    private long processingAssets;
    private long failedAssets;
    private long totalStorageBytes;
    private long readyStorageBytes;
    private long generatedToday;
    private long providerCharactersToday;
    private long cacheHitCount;
    private long enabledVoices;
    private long totalVoices;
}
