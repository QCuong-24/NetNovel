package com.example.netnovel_server.audio.service;

import org.springframework.core.io.Resource;

public interface AudioStorageService {

    String saveChapterAudio(Long chapterId, String fileName, byte[] audioBytes);

    Resource load(String storageKey);

    boolean exists(String storageKey);

    long size(String storageKey);

    void delete(String storageKey);
}
