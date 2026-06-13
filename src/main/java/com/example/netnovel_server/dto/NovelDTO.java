package com.example.netnovel_server.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelDTO {

    private Long novelId;

    private String title;

    private String author;

    private String description;

    private String coverImageUrl;

    private String coverImagePublicId;

    private Long views;

    private Long follows;

    private Long likes;

    private Set<String> tags;

    private String status;

    private Integer chapterCount;

    private Long latestChapterId;

    private Integer latestChapterNumber;

    private String latestChapterTitle;

    private LocalDateTime latestChapterUpdatedAt;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;
}
