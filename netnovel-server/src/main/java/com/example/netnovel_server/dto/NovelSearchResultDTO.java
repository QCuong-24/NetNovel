package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelSearchResultDTO {

    private NovelDTO novel;

    private Double score;
}
