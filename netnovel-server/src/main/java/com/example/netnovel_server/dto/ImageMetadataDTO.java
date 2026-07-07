package com.example.netnovel_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageMetadataDTO {

    @NotBlank(message = "Image URL is required")
    @Size(max = 1000, message = "Image URL must not exceed 1000 characters")
    private String url;

    @NotBlank(message = "Image public id is required")
    @Size(max = 255, message = "Image public id must not exceed 255 characters")
    private String publicId;
}
