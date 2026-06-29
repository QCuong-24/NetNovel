package com.example.netnovel_server.audio.repository;

import com.example.netnovel_server.audio.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterAudioAssetRepository extends JpaRepository<ChapterAudioAsset, Long> {

    Optional<ChapterAudioAsset> findFirstByChapter_IdAndProviderAndLanguageCodeAndVoiceNameAndEngineAndSpeakingRateAndPitchAndAudioEncodingAndContentHashAndStatus(
        Long chapterId,
        AudioProvider provider,
        String languageCode,
        String voiceName,
        String engine,
        BigDecimal speakingRate,
        BigDecimal pitch,
        AudioEncoding audioEncoding,
        String contentHash,
        ChapterAudioStatus status
    );

    Optional<ChapterAudioAsset> findFirstByChapter_IdAndProviderAndLanguageCodeAndVoiceNameAndEngineAndSpeakingRateAndPitchAndAudioEncodingAndContentHash(
        Long chapterId,
        AudioProvider provider,
        String languageCode,
        String voiceName,
        String engine,
        BigDecimal speakingRate,
        BigDecimal pitch,
        AudioEncoding audioEncoding,
        String contentHash
    );

    List<ChapterAudioAsset> findTop100ByStatusAndUpdatedAtBefore(
        ChapterAudioStatus status,
        LocalDateTime updatedBefore
    );

    List<ChapterAudioAsset> findTop100ByStatusAndLastAccessedAtBefore(
        ChapterAudioStatus status,
        LocalDateTime accessedBefore
    );

    List<ChapterAudioAsset> findTop100ByExpiresAtBefore(LocalDateTime expiresBefore);

    Page<ChapterAudioAsset> findByStatusOrderByUpdatedAtDesc(ChapterAudioStatus status, Pageable pageable);

    Page<ChapterAudioAsset> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    long countByStatus(ChapterAudioStatus status);

    long countByCreatedAtAfter(LocalDateTime createdAfter);

    @Query("select coalesce(sum(a.fileSizeBytes), 0) from ChapterAudioAsset a where a.status = com.example.netnovel_server.audio.entity.ChapterAudioStatus.READY")
    long sumReadyFileSizeBytes();

    @Query("select coalesce(sum(a.fileSizeBytes), 0) from ChapterAudioAsset a")
    long sumFileSizeBytes();

    @Query("select coalesce(sum(a.providerCharacterCount), 0) from ChapterAudioAsset a where a.createdAt >= :createdAfter")
    long sumProviderCharacterCountSince(LocalDateTime createdAfter);

    @Query("select coalesce(sum(a.cacheHitCount), 0) from ChapterAudioAsset a")
    long sumCacheHitCount();
}
