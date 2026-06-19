package com.example.netnovel_server.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelStatisticDTO {

    private String metric;

    private String period;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long count;
}
