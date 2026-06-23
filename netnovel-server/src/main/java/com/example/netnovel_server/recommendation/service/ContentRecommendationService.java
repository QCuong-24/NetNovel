package com.example.netnovel_server.recommendation.service;

import com.example.netnovel_server.entity.Genre;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Tag;
import com.example.netnovel_server.recommendation.entity.UserNovelInteraction;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContentRecommendationService {

    private static final double GENRE_WEIGHT = 1.5;
    private static final double TAG_WEIGHT = 2.0;
    private static final double AUTHOR_WEIGHT = 0.75;

    public Map<Long, Double> scoreCandidates(
        List<UserNovelInteraction> interactions,
        List<Novel> candidates
    ) {
        Map<String, Double> profile = new HashMap<>();
        interactions.forEach(interaction -> addToProfile(profile, interaction));

        Map<Long, Double> scores = new HashMap<>();
        for (Novel candidate : candidates) {
            List<Feature> features = featuresOf(candidate);
            double score = features.isEmpty()
                ? 0.0
                : features.stream().mapToDouble(feature -> profile.getOrDefault(feature.key(), 0.0)).average().orElse(0.0);
            scores.put(candidate.getId(), score);
        }
        return scores;
    }

    private void addToProfile(Map<String, Double> profile, UserNovelInteraction interaction) {
        double interactionWeight = interaction.getInteractionScore();
        for (Feature feature : featuresOf(interaction.getNovel())) {
            profile.merge(feature.key(), interactionWeight * feature.weight(), Double::sum);
        }
    }

    static List<Feature> featuresOf(Novel novel) {
        List<Feature> features = new java.util.ArrayList<>();
        novel.getGenres().forEach(genre -> features.add(new Feature("genre:" + genre.getName(), GENRE_WEIGHT)));
        novel.getTags().forEach(tag -> features.add(new Feature("tag:" + tag.getName(), TAG_WEIGHT)));
        if (novel.getAuthor() != null && !novel.getAuthor().isBlank()) {
            features.add(new Feature("author:" + novel.getAuthor().trim(), AUTHOR_WEIGHT));
        }
        return features;
    }

    record Feature(String key, double weight) {
    }
}
