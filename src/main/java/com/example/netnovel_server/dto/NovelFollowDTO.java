package com.example.netnovel_server.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelFollowDTO {

    private Long followId;

    private Long userId;

    private NovelDTO novel;

    private LocalDateTime followedAt;
}
