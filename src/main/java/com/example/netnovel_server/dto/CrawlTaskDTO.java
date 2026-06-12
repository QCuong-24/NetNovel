package com.example.netnovel_server.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlTaskDTO {

    private Long id;

    private String url;

    private String status;

    private Long requestedByUserId;

    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;
}
