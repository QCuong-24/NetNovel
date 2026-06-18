package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterCreateDTO {

    private String title;

    private Integer chapterNumber;

    private String content;
}
