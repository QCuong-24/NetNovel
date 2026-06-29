package com.example.netnovel_server.search.elastic.document;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElasticNovelDocument {

    private Long novelId;

    private String title;

    private String author;

    private String description;

    private Set<String> genres;

    private Set<String> tags;

    private String status;

    private Long views;

    private Long follows;

    private Long likes;

    private Long bookmarks;

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

    private String embeddingText;

    private List<Double> embeddingVector;

    private String embeddingModel;

    private Integer embeddingDimension;

    private LocalDateTime embeddingUpdatedAt;
}
