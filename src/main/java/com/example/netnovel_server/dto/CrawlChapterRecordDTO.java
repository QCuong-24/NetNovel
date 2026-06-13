package com.example.netnovel_server.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlChapterRecordDTO {

    private Long id;

    private String sourceName;

    private String sourceChapterUrl;

    private Long novelId;

    private String novelTitle;

    private Long chapterId;

    private String chapterTitle;

    private Integer chapterNumber;

    private String status;

    private String errorMessage;

    private LocalDateTime crawledAt;
}
