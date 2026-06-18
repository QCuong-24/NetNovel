package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelRankingDTO {

    private NovelDTO novel;

    private Long count;
}
