package com.example.netnovel_server.audio.service;

import com.example.netnovel_server.audio.dto.AdminAudioAssetDTO;
import com.example.netnovel_server.audio.dto.AdminAudioDashboardDTO;
import com.example.netnovel_server.audio.entity.ChapterAudioAsset;
import com.example.netnovel_server.audio.entity.ChapterAudioStatus;
import com.example.netnovel_server.audio.repository.AudioVoiceRepository;
import com.example.netnovel_server.audio.repository.ChapterAudioAssetRepository;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminAudioService {

    private final ChapterAudioAssetRepository audioAssetRepository;
    private final AudioVoiceRepository audioVoiceRepository;
    private final AudioStorageService audioStorageService;
    private final ChapterAudioService chapterAudioService;

    public AdminAudioService(
        ChapterAudioAssetRepository audioAssetRepository,
        AudioVoiceRepository audioVoiceRepository,
        AudioStorageService audioStorageService,
        ChapterAudioService chapterAudioService
    ) {
        this.audioAssetRepository = audioAssetRepository;
        this.audioVoiceRepository = audioVoiceRepository;
        this.audioStorageService = audioStorageService;
        this.chapterAudioService = chapterAudioService;
    }

    @Transactional(readOnly = true)
    public AdminAudioDashboardDTO getDashboard() {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();

        return AdminAudioDashboardDTO.builder()
            .totalAssets(audioAssetRepository.count())
            .readyAssets(audioAssetRepository.countByStatus(ChapterAudioStatus.READY))
            .processingAssets(audioAssetRepository.countByStatus(ChapterAudioStatus.PROCESSING))
            .failedAssets(audioAssetRepository.countByStatus(ChapterAudioStatus.FAILED))
            .totalStorageBytes(audioAssetRepository.sumFileSizeBytes())
            .readyStorageBytes(audioAssetRepository.sumReadyFileSizeBytes())
            .generatedToday(audioAssetRepository.countByCreatedAtAfter(today))
            .providerCharactersToday(audioAssetRepository.sumProviderCharacterCountSince(today))
            .cacheHitCount(audioAssetRepository.sumCacheHitCount())
            .enabledVoices(audioVoiceRepository.findByEnabledTrueOrderByLanguageCodeAscSortOrderAscVoiceNameAsc().size())
            .totalVoices(audioVoiceRepository.count())
            .build();
    }

    @Transactional(readOnly = true)
    public Page<AdminAudioAssetDTO> getAssets(ChapterAudioStatus status, Pageable pageable) {
        Page<ChapterAudioAsset> page = status == null
            ? audioAssetRepository.findAllByOrderByUpdatedAtDesc(pageable)
            : audioAssetRepository.findByStatusOrderByUpdatedAtDesc(status, pageable);

        return page.map(this::toDTO);
    }

    @Transactional
    public void retry(Long assetId) {
        chapterAudioService.retryAudioAsset(assetId);
    }

    @Transactional
    public void delete(Long assetId) {
        ChapterAudioAsset asset = findAsset(assetId);
        audioStorageService.delete(asset.getStorageKey());
        audioAssetRepository.delete(asset);
    }

    @Transactional
    public int cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        var expiredAssets = audioAssetRepository.findTop100ByExpiresAtBefore(now);
        expiredAssets.forEach((asset) -> {
            audioStorageService.delete(asset.getStorageKey());
            audioAssetRepository.delete(asset);
        });

        return expiredAssets.size();
    }

    private ChapterAudioAsset findAsset(Long assetId) {
        return audioAssetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Audio asset not found"));
    }

    private AdminAudioAssetDTO toDTO(ChapterAudioAsset asset) {
        var chapter = asset.getChapter();
        var novel = chapter.getNovel();

        return AdminAudioAssetDTO.builder()
            .assetId(asset.getId())
            .chapterId(chapter.getId())
            .novelId(novel.getId())
            .chapterTitle(chapter.getTitle())
            .novelTitle(novel.getTitle())
            .status(asset.getStatus())
            .provider(asset.getProvider())
            .languageCode(asset.getLanguageCode())
            .voiceName(asset.getVoiceName())
            .engine(asset.getEngine())
            .requestedByUserId(asset.getRequestedByUserId())
            .sourceTextCharacters(asset.getSourceTextCharacters())
            .chunkCount(asset.getChunkCount())
            .providerRequestCount(asset.getProviderRequestCount())
            .providerCharacterCount(asset.getProviderCharacterCount())
            .fileSizeBytes(asset.getFileSizeBytes())
            .generationDurationMs(asset.getGenerationDurationMs())
            .cacheHitCount(asset.getCacheHitCount())
            .retryCount(asset.getRetryCount())
            .lastErrorCode(asset.getLastErrorCode())
            .errorMessage(asset.getErrorMessage())
            .startedAt(asset.getStartedAt())
            .finishedAt(asset.getFinishedAt())
            .lastAccessedAt(asset.getLastAccessedAt())
            .expiresAt(asset.getExpiresAt())
            .createdAt(asset.getCreatedAt())
            .updatedAt(asset.getUpdatedAt())
            .build();
    }
}
