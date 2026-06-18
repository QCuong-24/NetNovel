package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelLastReadUpdateDTO {

    private Long novelId;

    private Long chapterId;
}
