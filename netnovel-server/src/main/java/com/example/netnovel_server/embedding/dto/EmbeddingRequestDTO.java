package com.example.netnovel_server.embedding.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbeddingRequestDTO {

    private List<String> texts;

    private String inputType;
}
