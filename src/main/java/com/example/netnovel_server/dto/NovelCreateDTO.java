package com.example.netnovel_server.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelCreateDTO {

    private String title;

    private String author;

    private String description;

    private String coverImageUrl;

    private String coverImagePublicId;

    private Set<String> tags;

    private String status;
}
