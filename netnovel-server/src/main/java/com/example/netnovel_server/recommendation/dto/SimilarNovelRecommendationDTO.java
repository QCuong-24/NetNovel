package com.example.netnovel_server.recommendation.dto;

import com.example.netnovel_server.dto.NovelDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimilarNovelRecommendationDTO {

    private NovelDTO novel;

    private Double score;

    private Double semanticScore;

    private Double contentScore;

    private Double popularityScore;

    private List<String> reasons;
}
