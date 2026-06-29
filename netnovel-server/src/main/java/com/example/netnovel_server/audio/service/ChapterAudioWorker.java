package com.example.netnovel_server.audio.service;

import com.example.netnovel_server.audio.dto.ChapterAudioJobMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ChapterAudioWorker {

    private static final Logger log = LoggerFactory.getLogger(ChapterAudioWorker.class);

    private final ChapterAudioService chapterAudioService;

    public ChapterAudioWorker(ChapterAudioService chapterAudioService) {
        this.chapterAudioService = chapterAudioService;
    }

    @RabbitListener(queues = "${app.audio.rabbit.generation-queue:audio.generation}")
    public void generateChapterAudio(ChapterAudioJobMessage message) {
        if (message == null || message.getAssetId() == null) {
            return;
        }

        try {
            chapterAudioService.generateAudioAsset(message.getAssetId());
        } catch (RuntimeException exception) {
            log.warn("Audio generation job failed. assetId={}", message.getAssetId(), exception);
        }
    }
}
