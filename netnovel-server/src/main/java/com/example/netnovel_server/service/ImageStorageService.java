package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.CloudinaryUploadSignatureDTO;

public interface ImageStorageService {

    CloudinaryUploadSignatureDTO createUploadSignature(String folder);

    void delete(String publicId);
}
