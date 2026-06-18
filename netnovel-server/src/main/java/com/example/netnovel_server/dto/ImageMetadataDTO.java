package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageMetadataDTO {

    private String url;

    private String publicId;
}
