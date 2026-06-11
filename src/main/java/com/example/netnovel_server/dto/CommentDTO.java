package com.example.netnovel_server.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {

    private Long commentId;

    private Long novelId;

    private Long chapterId;

    private Integer chapterNumber;

    private Long userId;

    private String username;

    private String userAvatarUrl;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime lastActivityAt;
}
