package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelInteractionDTO {

    private Long novelId;

    private Boolean followed;

    private Boolean liked;

    private Boolean bookmarked;

    private Long views;

    private Long follows;

    private Long likes;

    private Long bookmarks;
}
