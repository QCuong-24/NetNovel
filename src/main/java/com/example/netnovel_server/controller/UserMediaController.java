package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.CloudinaryUploadSignatureDTO;
import com.example.netnovel_server.dto.ImageMetadataDTO;
import com.example.netnovel_server.dto.UserDTO;
import com.example.netnovel_server.service.UserMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@Tag(name = "User Media", description = "Current user media APIs")
public class UserMediaController {

    private final UserMediaService userMediaService;

    public UserMediaController(UserMediaService userMediaService) {
        this.userMediaService = userMediaService;
    }

    @GetMapping("/avatar/upload-signature")
    @Operation(summary = "Create a signed Cloudinary upload signature for current user's avatar")
    public ResponseEntity<CloudinaryUploadSignatureDTO> createAvatarUploadSignature() {
        return ResponseEntity.ok(userMediaService.createAvatarUploadSignature());
    }

    @PatchMapping("/avatar")
    @Operation(summary = "Save current user's avatar after frontend uploads it to Cloudinary")
    public ResponseEntity<UserDTO> updateMyAvatar(@RequestBody ImageMetadataDTO request) {
        return ResponseEntity.ok(userMediaService.updateMyAvatar(request));
    }
}
