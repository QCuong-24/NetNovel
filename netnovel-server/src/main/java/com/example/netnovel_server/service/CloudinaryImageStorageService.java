package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.CloudinaryUploadSignatureDTO;
import com.example.netnovel_server.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

@Service
public class CloudinaryImageStorageService implements ImageStorageService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryImageStorageService(
        @Value("${cloudinary.cloud-name}") String cloudName,
        @Value("${cloudinary.api-key}") String apiKey,
        @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public CloudinaryUploadSignatureDTO createUploadSignature(String folder) {
        validateConfiguration();
        if (folder == null || folder.isBlank()) {
            throw new BadRequestException("Cloudinary folder is required");
        }

        long timestamp = Instant.now().getEpochSecond();
        Map<String, String> params = new TreeMap<>();
        params.put("folder", folder);
        params.put("timestamp", String.valueOf(timestamp));

        return CloudinaryUploadSignatureDTO.builder()
            .cloudName(cloudName)
            .apiKey(apiKey)
            .folder(folder)
            .timestamp(timestamp)
            .signature(sign(params))
            .uploadUrl(uploadUrl())
            .build();
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        validateConfiguration();

        try {
            long timestamp = Instant.now().getEpochSecond();
            Map<String, String> params = new TreeMap<>();
            params.put("public_id", publicId);
            params.put("timestamp", String.valueOf(timestamp));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("api_key", apiKey);
            body.add("timestamp", timestamp);
            body.add("public_id", publicId);
            body.add("signature", sign(params));

            restTemplate.exchange(
                destroyUrl(),
                HttpMethod.POST,
                new HttpEntity<>(body, formHeaders()),
                Map.class
            );
        } catch (RestClientException exception) {
            throw new BadRequestException("Could not delete old image");
        }
    }

    private HttpHeaders formHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private String uploadUrl() {
        return "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
    }

    private String destroyUrl() {
        return "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy";
    }

    private void validateConfiguration() {
        if (cloudName == null || cloudName.isBlank()
            || apiKey == null || apiKey.isBlank()
            || apiSecret == null || apiSecret.isBlank()) {
            throw new BadRequestException("Cloudinary is not configured");
        }
    }

    private String sign(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        params.forEach((key, value) -> {
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(key).append("=").append(value);
        });
        builder.append(apiSecret);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(builder.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-1 is not available", exception);
        }
    }
}
