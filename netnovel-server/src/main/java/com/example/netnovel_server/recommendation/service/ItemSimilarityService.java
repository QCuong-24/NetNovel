package com.example.netnovel_server.recommendation.service;

import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.recommendation.entity.UserNovelInteraction;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSimilarityService {

    public Map<Long, Double> scoreCandidates(
        List<UserNovelInteraction> interactions,
        List<Novel> candidates
    ) {
        Map<Long, Double> scores = new HashMap<>();
        for (Novel candidate : candidates) {
            double score = interactions.stream()
                .mapToDouble(interaction -> interaction.getInteractionScore() * similarity(interaction.getNovel(), candidate))
                .sum();
            scores.put(candidate.getId(), score);
        }
        return scores;
    }

    private double similarity(Novel left, Novel right) {
        Map<String, Double> leftFeatures = toFeatureWeights(left);
        Map<String, Double> rightFeatures = toFeatureWeights(right);
        if (leftFeatures.isEmpty() || rightFeatures.isEmpty()) {
            return 0.0;
        }

        double intersection = leftFeatures.entrySet().stream()
            .filter(entry -> rightFeatures.containsKey(entry.getKey()))
            .mapToDouble(entry -> Math.min(entry.getValue(), rightFeatures.get(entry.getKey())))
            .sum();
        double union = leftFeatures.values().stream().mapToDouble(Double::doubleValue).sum()
            + rightFeatures.values().stream().mapToDouble(Double::doubleValue).sum()
            - intersection;

        return union == 0.0 ? 0.0 : intersection / union;
    }

    private Map<String, Double> toFeatureWeights(Novel novel) {
        Map<String, Double> features = new HashMap<>();
        for (ContentRecommendationService.Feature feature : ContentRecommendationService.featuresOf(novel)) {
            features.put(feature.key(), feature.weight());
        }
        return features;
    }
}
