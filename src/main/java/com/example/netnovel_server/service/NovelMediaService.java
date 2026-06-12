package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.CloudinaryUploadSignatureDTO;
import com.example.netnovel_server.dto.ImageMetadataDTO;
import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.repository.NovelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NovelMediaService {

    private static final String NOVEL_COVER_FOLDER = "netnovel/novel-covers";

    private final NovelRepository novelRepository;
    private final ImageStorageService imageStorageService;

    public NovelMediaService(NovelRepository novelRepository, ImageStorageService imageStorageService) {
        this.novelRepository = novelRepository;
        this.imageStorageService = imageStorageService;
    }

    public CloudinaryUploadSignatureDTO createCoverUploadSignature(Long novelId) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel not found");
        }

        return imageStorageService.createUploadSignature(NOVEL_COVER_FOLDER);
    }

    @Transactional
    public NovelDTO updateCover(Long novelId, ImageMetadataDTO request) {
        Novel novel = novelRepository.findById(novelId)
            .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));

        validateImageMetadata(request, NOVEL_COVER_FOLDER);

        imageStorageService.delete(novel.getCoverImagePublicId());

        novel.setCoverImageUrl(request.getUrl());
        novel.setCoverImagePublicId(request.getPublicId());

        return NovelMapper.toDTO(novelRepository.save(novel));
    }

    private void validateImageMetadata(ImageMetadataDTO request, String folder) {
        if (request == null || request.getUrl() == null || request.getUrl().isBlank()) {
            throw new BadRequestException("Image url is required");
        }
        if (request.getPublicId() == null || request.getPublicId().isBlank()) {
            throw new BadRequestException("Image publicId is required");
        }
        if (!request.getPublicId().startsWith(folder + "/")) {
            throw new BadRequestException("Image publicId must belong to folder: " + folder);
        }
    }
}
