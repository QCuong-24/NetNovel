package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudinaryUploadSignatureDTO {

    private String cloudName;

    private String apiKey;

    private String folder;

    private long timestamp;

    private String signature;

    private String uploadUrl;
}
