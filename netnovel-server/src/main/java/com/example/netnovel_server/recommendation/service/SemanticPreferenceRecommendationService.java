package com.example.netnovel_server.recommendation.service;

import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.recommendation.entity.UserNovelInteraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SemanticPreferenceRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(SemanticPreferenceRecommendationService.class);

    private static final int PROFILE_NOVEL_LIMIT = 5;
    private static final int SIMILAR_PER_PROFILE_NOVEL = 20;

    private final ObjectProvider<ElasticSemanticRecommendationService> semanticRecommendationServiceProvider;

    public SemanticPreferenceRecommendationService(
        ObjectProvider<ElasticSemanticRecommendationService> semanticRecommendationServiceProvider
    ) {
        this.semanticRecommendationServiceProvider = semanticRecommendationServiceProvider;
    }

    public Map<Long, Double> scoreCandidates(List<UserNovelInteraction> interactions) {
        ElasticSemanticRecommendationService service = semanticRecommendationServiceProvider.getIfAvailable();
        if (service == null || interactions == null || interactions.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> scores = new HashMap<>();
        interactions.stream()
            .filter(interaction -> interaction.getNovel() != null && interaction.getNovel().getId() != null)
            .limit(PROFILE_NOVEL_LIMIT)
            .forEach(interaction -> addSemanticScores(service, scores, interaction));
        return scores;
    }

    private void addSemanticScores(
        ElasticSemanticRecommendationService service,
        Map<Long, Double> scores,
        UserNovelInteraction interaction
    ) {
        Long sourceNovelId = interaction.getNovel().getId();
        double interactionWeight = Math.max(1.0, interaction.getInteractionScore());
        try {
            Page<NovelSearchResultDTO> similarPage = service.getSimilarNovels(
                sourceNovelId,
                PageRequest.of(0, SIMILAR_PER_PROFILE_NOVEL)
            );
            for (NovelSearchResultDTO result : similarPage.getContent()) {
                NovelDTO novel = result.getNovel();
                if (novel == null || novel.getNovelId() == null || novel.getNovelId().equals(sourceNovelId)) {
                    continue;
                }
                double semanticScore = result.getScore() == null ? 0.0 : result.getScore();
                scores.merge(novel.getNovelId(), semanticScore * interactionWeight, Double::sum);
            }
        } catch (Exception exception) {
            log.warn("Could not score semantic preferences from source novel. novelId={}", sourceNovelId, exception);
        }
    }
}
