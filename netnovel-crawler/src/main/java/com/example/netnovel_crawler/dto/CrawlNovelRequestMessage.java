package com.example.netnovel_crawler.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlNovelRequestMessage {

    private Long taskId;

    private String url;

    private Long requestedByUserId;
}
