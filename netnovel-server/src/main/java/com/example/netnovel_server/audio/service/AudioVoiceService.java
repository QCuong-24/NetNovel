package com.example.netnovel_server.audio.service;

import com.example.netnovel_server.audio.dto.AudioVoiceDTO;
import com.example.netnovel_server.audio.dto.AudioVoiceUpdateDTO;
import com.example.netnovel_server.audio.entity.AudioProvider;
import com.example.netnovel_server.audio.entity.AudioVoice;
import com.example.netnovel_server.audio.repository.AudioVoiceRepository;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.DescribeVoicesRequest;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.Voice;

import java.util.List;

@Service
public class AudioVoiceService {

    private final AudioVoiceRepository audioVoiceRepository;
    private final PollyClient pollyClient;

    public AudioVoiceService(AudioVoiceRepository audioVoiceRepository, PollyClient pollyClient) {
        this.audioVoiceRepository = audioVoiceRepository;
        this.pollyClient = pollyClient;
    }

    @Transactional(readOnly = true)
    public List<AudioVoiceDTO> getEnabledVoices(String languageCode) {
        List<AudioVoice> voices = languageCode == null || languageCode.isBlank()
            ? audioVoiceRepository.findByEnabledTrueOrderByLanguageCodeAscSortOrderAscVoiceNameAsc()
            : audioVoiceRepository.findByLanguageCodeAndEnabledTrueOrderBySortOrderAscVoiceNameAsc(languageCode);

        return voices.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<AudioVoiceDTO> getAdminVoices() {
        return audioVoiceRepository.findAllByOrderByLanguageCodeAscSortOrderAscVoiceNameAsc()
            .stream()
            .map(this::toDTO)
            .toList();
    }

    @Transactional
    public AudioVoiceDTO updateVoice(Long voiceId, AudioVoiceUpdateDTO request) {
        AudioVoice voice = audioVoiceRepository.findById(voiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Audio voice not found"));

        if (request.getEnabled() != null) {
            voice.setEnabled(request.getEnabled());
        }
        if (request.getDefaultVoice() != null) {
            voice.setDefaultVoice(request.getDefaultVoice());
        }
        if (request.getSortOrder() != null) {
            voice.setSortOrder(request.getSortOrder());
        }

        return toDTO(audioVoiceRepository.save(voice));
    }

    @Transactional
    public List<AudioVoiceDTO> syncPollyVoices() {
        for (Engine engine : List.of(Engine.STANDARD, Engine.NEURAL)) {
            List<Voice> pollyVoices = pollyClient.describeVoices(DescribeVoicesRequest.builder().engine(engine).build()).voices();
            for (Voice pollyVoice : pollyVoices) {
                String voiceName = pollyVoice.idAsString();
                String engineName = engine.toString().toLowerCase();
                AudioVoice voice = audioVoiceRepository
                    .findByProviderAndVoiceNameAndEngine(AudioProvider.AWS_POLLY, voiceName, engineName)
                    .orElseGet(() -> AudioVoice.builder()
                        .provider(AudioProvider.AWS_POLLY)
                        .voiceName(voiceName)
                        .engine(engineName)
                        .enabled(true)
                        .defaultVoice(false)
                        .sortOrder(0)
                        .build());

                voice.setLanguageCode(pollyVoice.languageCodeAsString());
                voice.setDisplayName(pollyVoice.name() == null || pollyVoice.name().isBlank() ? voiceName : pollyVoice.name());
                voice.setGender(pollyVoice.genderAsString());
                audioVoiceRepository.save(voice);
            }
        }

        return getAdminVoices();
    }

    private AudioVoiceDTO toDTO(AudioVoice voice) {
        return AudioVoiceDTO.builder()
            .id(voice.getId())
            .provider(voice.getProvider())
            .languageCode(voice.getLanguageCode())
            .voiceName(voice.getVoiceName())
            .displayName(voice.getDisplayName())
            .gender(voice.getGender())
            .engine(voice.getEngine())
            .enabled(voice.isEnabled())
            .defaultVoice(voice.isDefaultVoice())
            .sortOrder(voice.getSortOrder())
            .build();
    }
}
