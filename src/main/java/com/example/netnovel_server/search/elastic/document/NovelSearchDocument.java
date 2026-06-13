package com.example.netnovel_server.search.elastic.document;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelSearchDocument {

    private Long novelId;

    private String title;

    private String author;

    private String description;

    private Set<String> tags;

    private String status;

    private Long views;

    private Long follows;

    private Long likes;

    private Integer chapterCount;

    private Integer latestChapterNumber;

    private LocalDateTime lastChapterUpdatedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean crawled;

    private String sourceName;

    private String sourceNovelUrl;

    private Double popularityScore;

    private Double freshnessScore;

    private String recommendationText;
}
