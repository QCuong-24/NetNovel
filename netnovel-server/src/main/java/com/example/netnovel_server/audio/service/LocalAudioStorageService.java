package com.example.netnovel_server.audio.service;

import com.example.netnovel_server.audio.config.AudioProperties;
import com.example.netnovel_server.audio.exception.AudioStorageException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LocalAudioStorageService implements AudioStorageService {

    private final Path rootPath;

    public LocalAudioStorageService(AudioProperties properties) {
        this.rootPath = Path.of(properties.getStoragePath()).toAbsolutePath().normalize();
    }

    @Override
    public String saveChapterAudio(Long chapterId, String fileName, byte[] audioBytes) {
        String storageKey = "chapters/" + chapterId + "/" + fileName;
        Path target = resolve(storageKey);

        try {
            Files.createDirectories(target.getParent());
            Files.write(target, audioBytes);
            return storageKey;
        } catch (IOException exception) {
            throw new AudioStorageException("Could not save audio file", exception);
        }
    }

    @Override
    public Resource load(String storageKey) {
        Path file = resolve(storageKey);
        if (!Files.isRegularFile(file)) {
            throw new ResourceNotFoundException("Audio file is missing");
        }

        return new FileSystemResource(file);
    }

    @Override
    public boolean exists(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return false;
        }

        return Files.isRegularFile(resolve(storageKey));
    }

    @Override
    public long size(String storageKey) {
        try {
            return Files.size(resolve(storageKey));
        } catch (IOException exception) {
            throw new AudioStorageException("Could not inspect audio file", exception);
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException exception) {
            throw new AudioStorageException("Could not delete audio file", exception);
        }
    }

    private Path resolve(String storageKey) {
        Path resolved = rootPath.resolve(storageKey).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new AudioStorageException("Invalid audio storage key");
        }

        return resolved;
    }
}
