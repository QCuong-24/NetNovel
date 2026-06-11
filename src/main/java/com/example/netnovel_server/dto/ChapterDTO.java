package com.example.netnovel_server.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterDTO {

    private Long chapterId;

    private Long novelId;

    private String novelTitle;

    private String title;

    private Integer chapterNumber;

    private LocalDateTime updateAt;
}
