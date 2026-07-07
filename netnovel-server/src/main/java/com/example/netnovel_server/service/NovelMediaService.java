package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.CloudinaryUploadSignatureDTO;
import com.example.netnovel_server.dto.ImageMetadataDTO;
import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.utility.HtmlSanitizer;
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

        novel.setCoverImageUrl(HtmlSanitizer.safeUrlLikeText(request.getUrl()));
        novel.setCoverImagePublicId(HtmlSanitizer.safeUrlLikeText(request.getPublicId()));

        return NovelMapper.toDTO(novelRepository.save(novel));
    }

    private void validateImageMetadata(ImageMetadataDTO request, String folder) {
        if (request == null) {
            throw new BadRequestException("Image url is required");
        }
        String url = HtmlSanitizer.safeUrlLikeText(request.getUrl());
        String publicId = HtmlSanitizer.safeUrlLikeText(request.getPublicId());
        if (url == null || url.isBlank()) {
            throw new BadRequestException("Image url is required");
        }
        if (publicId == null || publicId.isBlank()) {
            throw new BadRequestException("Image publicId is required");
        }
        if (!publicId.startsWith(folder + "/")) {
            throw new BadRequestException("Image publicId must belong to folder: " + folder);
        }
        request.setUrl(url);
        request.setPublicId(publicId);
    }
}
