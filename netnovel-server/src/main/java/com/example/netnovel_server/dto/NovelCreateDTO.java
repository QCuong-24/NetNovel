package com.example.netnovel_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelCreateDTO {

    @NotBlank(message = "Novel title is required")
    @Size(max = 255, message = "Novel title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Novel author is required")
    @Size(max = 255, message = "Novel author must not exceed 255 characters")
    private String author;

    @Size(max = 10000, message = "Novel description must not exceed 10000 characters")
    private String description;

    @Size(max = 1000, message = "Cover image URL must not exceed 1000 characters")
    private String coverImageUrl;

    @Size(max = 255, message = "Cover image public id must not exceed 255 characters")
    private String coverImagePublicId;

    private Set<String> genres;

    private Set<String> tags;

    @NotBlank(message = "Novel status is required")
    private String status;

    private String accessStatus;
}
