package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.CloudinaryUploadSignatureDTO;
import com.example.netnovel_server.dto.ImageMetadataDTO;
import com.example.netnovel_server.dto.UserDTO;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.UserMapper;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.HtmlSanitizer;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserMediaService {

    private static final String AVATAR_FOLDER = "netnovel/avatars";

    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    public UserMediaService(UserRepository userRepository, ImageStorageService imageStorageService) {
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
    }

    public CloudinaryUploadSignatureDTO createAvatarUploadSignature() {
        return imageStorageService.createUploadSignature(AVATAR_FOLDER);
    }

    @Transactional
    public UserDTO updateMyAvatar(ImageMetadataDTO request) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateImageMetadata(request, AVATAR_FOLDER);

        imageStorageService.delete(user.getProfilePicturePublicId());

        user.setProfilePictureUrl(HtmlSanitizer.safeUrlLikeText(request.getUrl()));
        user.setProfilePicturePublicId(HtmlSanitizer.safeUrlLikeText(request.getPublicId()));

        return UserMapper.toDTO(userRepository.save(user));
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
