package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchSuggestionDTO {

    private String type;

    private Long id;

    private String label;
}
