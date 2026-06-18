package com.example.netnovel_server.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkDTO {

    private Long bookmarkId;

    private Long userId;

    private Long novelId;

    private String novelTitle;

    private String author;

    private String coverImageUrl;

    private Long chapterId;

    private String chapterTitle;

    private Integer chapterNumber;

    private LocalDateTime createdAt;
}
