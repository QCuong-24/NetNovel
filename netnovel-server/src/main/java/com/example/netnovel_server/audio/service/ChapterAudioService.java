package com.example.netnovel_server.audio.service;

import com.example.netnovel_server.audio.config.AudioProperties;
import com.example.netnovel_server.audio.dto.ChapterAudioRequestDTO;
import com.example.netnovel_server.audio.dto.ChapterAudioResponseDTO;
import com.example.netnovel_server.audio.entity.*;
import com.example.netnovel_server.audio.event.AudioGenerationRequestedEvent;
import com.example.netnovel_server.audio.repository.ChapterAudioAssetRepository;
import com.example.netnovel_server.dto.ChapterContentDTO;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.repository.ChapterRepository;
import com.example.netnovel_server.service.ChapterService;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class ChapterAudioService {

    private static final String AUDIO_MPEG = "audio/mpeg";

    private final ChapterService chapterService;
    private final ChapterRepository chapterRepository;
    private final ChapterAudioAssetRepository audioAssetRepository;
    private final PollyTtsService pollyTtsService;
    private final AudioStorageService audioStorageService;
    private final AudioQuotaService audioQuotaService;
    private final AudioProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    public ChapterAudioService(
        ChapterService chapterService,
        ChapterRepository chapterRepository,
        ChapterAudioAssetRepository audioAssetRepository,
        PollyTtsService pollyTtsService,
        AudioStorageService audioStorageService,
        AudioQuotaService audioQuotaService,
        AudioProperties properties,
        ApplicationEventPublisher eventPublisher
    ) {
        this.chapterService = chapterService;
        this.chapterRepository = chapterRepository;
        this.audioAssetRepository = audioAssetRepository;
        this.pollyTtsService = pollyTtsService;
        this.audioStorageService = audioStorageService;
        this.audioQuotaService = audioQuotaService;
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ChapterAudioResponseDTO createOrGetChapterAudio(Long chapterId, ChapterAudioRequestDTO request) {
        Long currentUserId = SecurityUtils.getCurrentUserIdOrThrow();
        AudioOptions options = resolveOptions(request);
        ChapterContentDTO chapter = chapterService.getChapter(chapterId);
        String content = normalizeContent(chapter.getContent());
        validateContent(content);

        String contentHash = hashContent(content, options);
        return audioAssetRepository
            .findFirstByChapter_IdAndProviderAndLanguageCodeAndVoiceNameAndEngineAndSpeakingRateAndPitchAndAudioEncodingAndContentHash(
                chapterId,
                options.provider(),
                options.languageCode(),
                options.voiceName(),
                options.engine(),
                options.speakingRate(),
                options.pitch(),
                options.audioEncoding(),
                contentHash
            )
            .map((asset) -> handleExistingAsset(asset, content, currentUserId))
            .orElseGet(() -> {
                audioQuotaService.reserveMonthlyCharacters(currentUserId, content.length());
                return enqueueAsset(createProcessingAsset(chapter.getChapterId(), options, contentHash, content.length(), currentUserId));
            });
    }

    @Transactional(readOnly = true)
    public ChapterAudioResponseDTO getAudioAsset(Long assetId) {
        ChapterAudioAsset asset = audioAssetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Audio asset not found"));
        chapterService.getChapter(asset.getChapter().getId());

        return toResponse(asset, false);
    }

    @Transactional
    public void generateAudioAsset(Long assetId) {
        ChapterAudioAsset asset = audioAssetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Audio asset not found"));

        if (asset.getStatus() == ChapterAudioStatus.READY || asset.getStatus() == ChapterAudioStatus.CANCELLED) {
            return;
        }

        ChapterContentDTO chapter = chapterService.getChapter(asset.getChapter().getId());
        String content = normalizeContent(chapter.getContent());
        validateContent(content);
        AudioOptions options = toOptions(asset);

        generateAndStore(asset, chapter, content, options);
    }

    @Transactional
    public ChapterAudioResponseDTO retryAudioAsset(Long assetId) {
        ChapterAudioAsset asset = audioAssetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Audio asset not found"));
        asset.setRetryCount((asset.getRetryCount() == null ? 0 : asset.getRetryCount()) + 1);

        return enqueueAsset(asset);
    }

    @Transactional(readOnly = true)
    public Resource loadAudioFile(Long assetId) {
        ChapterAudioAsset asset = findReadyAsset(assetId);
        return audioStorageService.load(asset.getStorageKey());
    }

    @Transactional(readOnly = true)
    public long getAudioFileSize(Long assetId) {
        ChapterAudioAsset asset = findReadyAsset(assetId);
        if (asset.getFileSizeBytes() != null) {
            return asset.getFileSizeBytes();
        }

        return audioStorageService.size(asset.getStorageKey());
    }

    @Transactional
    public void markAccessed(Long assetId) {
        ChapterAudioAsset asset = findReadyAsset(assetId);
        asset.setLastAccessedAt(LocalDateTime.now());
        audioAssetRepository.save(asset);
    }

    @Transactional
    protected ChapterAudioResponseDTO markCacheHit(ChapterAudioAsset asset, Long currentUserId) {
        asset.setLastAccessedAt(LocalDateTime.now());
        asset.setCacheHitCount((asset.getCacheHitCount() == null ? 0L : asset.getCacheHitCount()) + 1);
        audioQuotaService.recordCacheHit(currentUserId);
        return toResponse(audioAssetRepository.save(asset), true);
    }

    private ChapterAudioResponseDTO handleExistingAsset(ChapterAudioAsset asset, String content, Long currentUserId) {
        if (asset.getStatus() == ChapterAudioStatus.READY) {
            if (!audioStorageService.exists(asset.getStorageKey())) {
                audioQuotaService.reserveMonthlyCharacters(currentUserId, content.length());
                asset.setStatus(ChapterAudioStatus.EXPIRED);
                asset.setLastErrorCode("AUDIO_FILE_MISSING");
                asset.setErrorMessage("Audio file is missing from local storage");
                asset.setStorageKey(null);
                asset.setFileSizeBytes(null);
                asset.setFinishedAt(null);
                asset.setRequestedByUserId(currentUserId);
                asset.setSourceTextCharacters(content.length());
                audioAssetRepository.save(asset);
                return enqueueAsset(asset);
            }

            return markCacheHit(asset, currentUserId);
        }

        if (asset.getStatus() == ChapterAudioStatus.PROCESSING) {
            return toResponse(asset, false);
        }

        audioQuotaService.reserveMonthlyCharacters(currentUserId, content.length());
        asset.setSourceTextCharacters(content.length());
        asset.setRequestedByUserId(currentUserId);
        asset.setRetryCount((asset.getRetryCount() == null ? 0 : asset.getRetryCount()) + 1);
        return enqueueAsset(asset);
    }

    private ChapterAudioResponseDTO enqueueAsset(ChapterAudioAsset asset) {
        asset.setStatus(ChapterAudioStatus.PROCESSING);
        asset.setErrorMessage(null);
        asset.setLastErrorCode(null);
        asset.setStartedAt(null);
        asset.setFinishedAt(null);
        asset.setStorageType(properties.getStorageType());
        asset.setAudioUrl("/audio-assets/" + asset.getId() + "/file");
        ChapterAudioAsset savedAsset = audioAssetRepository.save(asset);
        eventPublisher.publishEvent(new AudioGenerationRequestedEvent(savedAsset.getId()));

        return toResponse(savedAsset, false);
    }

    private ChapterAudioResponseDTO generateAndStore(
        ChapterAudioAsset asset,
        ChapterContentDTO chapter,
        String content,
        AudioOptions options
    ) {
        asset.setStatus(ChapterAudioStatus.PROCESSING);
        asset.setErrorMessage(null);
        asset.setLastErrorCode(null);
        asset.setStartedAt(LocalDateTime.now());
        asset.setStorageType(properties.getStorageType());
        asset = audioAssetRepository.save(asset);

        try {
            long startedAt = System.currentTimeMillis();
            List<String> chunks = splitIntoChunks(content);
            byte[] audioBytes = pollyTtsService.synthesize(
                chunks,
                options.voiceName(),
                options.engine(),
                options.speakingRate(),
                options.pitch(),
                options.audioEncoding()
            );
            String fileName = asset.getContentHash() + "-" + safeFilePart(options.voiceName()) + ".mp3";
            String storageKey = audioStorageService.saveChapterAudio(chapter.getChapterId(), fileName, audioBytes);
            LocalDateTime finishedAt = LocalDateTime.now();

            asset.setStatus(ChapterAudioStatus.READY);
            asset.setStorageKey(storageKey);
            asset.setAudioUrl("/audio-assets/" + asset.getId() + "/file");
            asset.setMimeType(AUDIO_MPEG);
            asset.setFileSizeBytes((long) audioBytes.length);
            asset.setSourceTextCharacters(content.length());
            asset.setChunkCount(chunks.size());
            asset.setProviderRequestCount(chunks.size());
            asset.setProviderCharacterCount(content.length());
            asset.setGenerationDurationMs(System.currentTimeMillis() - startedAt);
            asset.setErrorMessage(null);
            asset.setLastAccessedAt(LocalDateTime.now());
            asset.setFinishedAt(finishedAt);
            asset.setExpiresAt(LocalDateTime.now().plusDays(properties.getCacheTtlDays()));
            audioQuotaService.recordGeneratedCharacters(asset.getRequestedByUserId(), content.length());

            return toResponse(audioAssetRepository.save(asset), false);
        } catch (RuntimeException exception) {
            audioQuotaService.releaseReservedCharacters(asset.getRequestedByUserId(), content.length());
            asset.setStatus(ChapterAudioStatus.FAILED);
            asset.setLastErrorCode(exception.getClass().getSimpleName());
            asset.setErrorMessage(exception.getMessage());
            asset.setFinishedAt(LocalDateTime.now());
            audioAssetRepository.save(asset);
            throw exception;
        }
    }

    private ChapterAudioAsset createProcessingAsset(
        Long chapterId,
        AudioOptions options,
        String contentHash,
        int sourceTextCharacters,
        Long currentUserId
    ) {
        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        ChapterAudioAsset asset = ChapterAudioAsset.builder()
            .chapter(chapter)
            .provider(options.provider())
            .languageCode(options.languageCode())
            .voiceName(options.voiceName())
            .engine(options.engine())
            .speakingRate(options.speakingRate())
            .pitch(options.pitch())
            .audioEncoding(options.audioEncoding())
            .contentHash(contentHash)
            .status(ChapterAudioStatus.PROCESSING)
            .storageType(properties.getStorageType())
            .mimeType(AUDIO_MPEG)
            .requestedByUserId(currentUserId)
            .sourceTextCharacters(sourceTextCharacters)
            .build();

        return audioAssetRepository.save(asset);
    }

    private ChapterAudioAsset findReadyAsset(Long assetId) {
        ChapterAudioAsset asset = audioAssetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Audio asset not found"));

        if (asset.getStatus() != ChapterAudioStatus.READY || asset.getStorageKey() == null || asset.getStorageKey().isBlank()) {
            throw new ResourceNotFoundException("Audio asset not found");
        }

        chapterService.getChapter(asset.getChapter().getId());
        return asset;
    }

    private AudioOptions resolveOptions(ChapterAudioRequestDTO request) {
        String languageCode = valueOrDefault(request != null ? request.getLanguageCode() : null, properties.getDefaultLanguageCode());
        String voiceName = valueOrDefault(request != null ? request.getVoiceName() : null, properties.getDefaultVoiceName());
        String engine = valueOrDefault(request != null ? request.getEngine() : null, properties.getDefaultEngine());
        BigDecimal speakingRate = normalizeDecimal(
            request != null && request.getSpeakingRate() != null
                ? request.getSpeakingRate()
                : properties.getDefaultSpeakingRate()
        );
        BigDecimal pitch = normalizeDecimal(
            request != null && request.getPitch() != null
                ? request.getPitch()
                : properties.getDefaultPitch()
        );
        AudioEncoding audioEncoding = request != null && request.getAudioEncoding() != null && !request.getAudioEncoding().isBlank()
            ? AudioEncoding.valueOf(request.getAudioEncoding().toUpperCase())
            : properties.getDefaultAudioEncoding();

        if (speakingRate.compareTo(new BigDecimal("0.50")) < 0 || speakingRate.compareTo(new BigDecimal("2.00")) > 0) {
            throw new BadRequestException("Speaking rate must be between 0.50 and 2.00");
        }

        if (pitch.compareTo(new BigDecimal("-50.00")) < 0 || pitch.compareTo(new BigDecimal("50.00")) > 0) {
            throw new BadRequestException("Pitch must be between -50 and 50");
        }

        return new AudioOptions(
            properties.getDefaultProvider(),
            languageCode,
            voiceName,
            engine,
            speakingRate,
            pitch,
            audioEncoding
        );
    }

    private AudioOptions toOptions(ChapterAudioAsset asset) {
        return new AudioOptions(
            asset.getProvider(),
            asset.getLanguageCode(),
            asset.getVoiceName(),
            asset.getEngine(),
            asset.getSpeakingRate(),
            asset.getPitch(),
            asset.getAudioEncoding()
        );
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private BigDecimal normalizeDecimal(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeContent(String content) {
        return content == null ? "" : content.replace("\r\n", "\n").replace("\r", "\n").trim();
    }

    private void validateContent(String content) {
        if (content.isBlank()) {
            throw new BadRequestException("Chapter content is empty");
        }

        if (content.length() > properties.getMaxTotalCharacters()) {
            throw new BadRequestException("Chapter content is too long for audio generation");
        }
    }

    private List<String> splitIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = content.split("\\n{2,}");
        StringBuilder current = new StringBuilder();

        for (String paragraph : paragraphs) {
            String normalizedParagraph = paragraph.trim();
            if (normalizedParagraph.isBlank()) {
                continue;
            }

            if (normalizedParagraph.length() > properties.getMaxChunkCharacters()) {
                flushChunk(chunks, current);
                splitLongParagraph(chunks, normalizedParagraph);
                continue;
            }

            if (current.length() + normalizedParagraph.length() + 2 > properties.getMaxChunkCharacters()) {
                flushChunk(chunks, current);
            }

            if (!current.isEmpty()) {
                current.append("\n\n");
            }
            current.append(normalizedParagraph);
        }

        flushChunk(chunks, current);
        return chunks;
    }

    private void splitLongParagraph(List<String> chunks, String paragraph) {
        String[] sentences = paragraph.split("(?<=[.!?。！？])\\s+");
        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            if (sentence.length() > properties.getMaxChunkCharacters()) {
                flushChunk(chunks, current);
                splitByLength(chunks, sentence);
                continue;
            }

            if (current.length() + sentence.length() + 1 > properties.getMaxChunkCharacters()) {
                flushChunk(chunks, current);
            }

            if (!current.isEmpty()) {
                current.append(" ");
            }
            current.append(sentence);
        }

        flushChunk(chunks, current);
    }

    private void splitByLength(List<String> chunks, String text) {
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + properties.getMaxChunkCharacters(), text.length());
            chunks.add(text.substring(start, end).trim());
            start = end;
        }
    }

    private void flushChunk(List<String> chunks, StringBuilder current) {
        if (!current.isEmpty()) {
            chunks.add(current.toString());
            current.setLength(0);
        }
    }

    private String hashContent(String content, AudioOptions options) {
        String cacheInput = String.join("|",
            content,
            options.provider().name(),
            options.languageCode(),
            options.voiceName(),
            options.engine(),
            options.speakingRate().toPlainString(),
            options.pitch().toPlainString(),
            options.audioEncoding().name()
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(cacheInput.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String safeFilePart(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "-");
    }

    private ChapterAudioResponseDTO toResponse(ChapterAudioAsset asset, boolean cached) {
        return ChapterAudioResponseDTO.builder()
            .assetId(asset.getId())
            .chapterId(asset.getChapter().getId())
            .status(asset.getStatus())
            .audioUrl(asset.getAudioUrl())
            .cached(cached)
            .provider(asset.getProvider())
            .languageCode(asset.getLanguageCode())
            .voiceName(asset.getVoiceName())
            .engine(asset.getEngine())
            .audioEncoding(asset.getAudioEncoding().name())
            .fileSizeBytes(asset.getFileSizeBytes())
            .durationMs(asset.getDurationMs())
            .errorMessage(asset.getErrorMessage())
            .expiresAt(asset.getExpiresAt())
            .build();
    }

    private record AudioOptions(
        AudioProvider provider,
        String languageCode,
        String voiceName,
        String engine,
        BigDecimal speakingRate,
        BigDecimal pitch,
        AudioEncoding audioEncoding
    ) {
    }
}
