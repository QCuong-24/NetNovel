package com.example.netnovel_server.audio.repository;

import com.example.netnovel_server.audio.entity.AudioProvider;
import com.example.netnovel_server.audio.entity.AudioVoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AudioVoiceRepository extends JpaRepository<AudioVoice, Long> {

    List<AudioVoice> findByEnabledTrueOrderByLanguageCodeAscSortOrderAscVoiceNameAsc();

    List<AudioVoice> findByLanguageCodeAndEnabledTrueOrderBySortOrderAscVoiceNameAsc(String languageCode);

    List<AudioVoice> findAllByOrderByLanguageCodeAscSortOrderAscVoiceNameAsc();

    Optional<AudioVoice> findByProviderAndVoiceNameAndEngine(AudioProvider provider, String voiceName, String engine);
}
