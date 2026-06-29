package com.example.netnovel_server.audio.service;

import com.example.netnovel_server.audio.entity.ChapterAudioAsset;
import com.example.netnovel_server.audio.entity.ChapterAudioStatus;
import com.example.netnovel_server.audio.repository.ChapterAudioAssetRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class AudioCleanupService {

    private final ChapterAudioAssetRepository audioAssetRepository;
    private final AudioStorageService audioStorageService;

    public AudioCleanupService(ChapterAudioAssetRepository audioAssetRepository, AudioStorageService audioStorageService) {
        this.audioAssetRepository = audioAssetRepository;
        this.audioStorageService = audioStorageService;
    }

    @Scheduled(fixedDelayString = "${app.audio.cleanup.fixed-delay-ms:3600000}")
    @Transactional
    public void cleanupExpiredAudio() {
        LocalDateTime now = LocalDateTime.now();
        Set<ChapterAudioAsset> removableAssets = new LinkedHashSet<>();
        removableAssets.addAll(audioAssetRepository.findTop100ByStatusAndUpdatedAtBefore(
            ChapterAudioStatus.FAILED,
            now.minusDays(1)
        ));
        removableAssets.addAll(audioAssetRepository.findTop100ByStatusAndLastAccessedAtBefore(
            ChapterAudioStatus.READY,
            now.minusDays(30)
        ));
        removableAssets.addAll(audioAssetRepository.findTop100ByExpiresAtBefore(now));

        for (ChapterAudioAsset asset : removableAssets) {
            audioStorageService.delete(asset.getStorageKey());
            audioAssetRepository.delete(asset);
        }
    }
}
