package com.example.netnovel_server.recommendation.service;

import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.entity.Genre;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.recommendation.dto.RecommendationItemDTO;
import com.example.netnovel_server.recommendation.entity.UserNovelInteraction;
import com.example.netnovel_server.recommendation.repository.UserNovelInteractionRepository;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class HybridRecommendationService {

    private static final int MAX_SIZE = 50;
    private static final double CONTENT_WEIGHT = 0.50;
    private static final double ITEM_SIMILARITY_WEIGHT = 0.35;
    private static final double POPULARITY_FRESHNESS_WEIGHT = 0.15;
    private static final double SEMANTIC_CONTENT_WEIGHT = 0.30;
    private static final double SEMANTIC_ITEM_SIMILARITY_WEIGHT = 0.30;
    private static final double SEMANTIC_PREFERENCE_WEIGHT = 0.25;
    private static final double SEMANTIC_POPULARITY_FRESHNESS_WEIGHT = 0.15;
    private static final int MAX_PER_AUTHOR = 2;
    private static final int MAX_PER_PRIMARY_GENRE = 3;

    private final NovelRepository novelRepository;
    private final UserNovelInteractionRepository interactionRepository;
    private final ContentRecommendationService contentRecommendationService;
    private final ItemSimilarityService itemSimilarityService;
    private final SemanticPreferenceRecommendationService semanticPreferenceRecommendationService;

    public HybridRecommendationService(
        NovelRepository novelRepository,
        UserNovelInteractionRepository interactionRepository,
        ContentRecommendationService contentRecommendationService,
        ItemSimilarityService itemSimilarityService,
        SemanticPreferenceRecommendationService semanticPreferenceRecommendationService
    ) {
        this.novelRepository = novelRepository;
        this.interactionRepository = interactionRepository;
        this.contentRecommendationService = contentRecommendationService;
        this.itemSimilarityService = itemSimilarityService;
        this.semanticPreferenceRecommendationService = semanticPreferenceRecommendationService;
    }

    @Transactional(readOnly = true)
    public List<RecommendationItemDTO> getForCurrentUser(int requestedSize) {
        int size = Math.min(Math.max(requestedSize, 1), MAX_SIZE);
        List<UserNovelInteraction> interactions = SecurityUtils.getCurrentUserId()
            .map(interactionRepository::findByUserIdOrderByInteractionScoreDesc)
            .orElseGet(List::of);
        Set<Long> interactedNovelIds = interactions.stream()
            .map(interaction -> interaction.getNovel().getId())
            .collect(java.util.stream.Collectors.toSet());
        List<Novel> candidates = novelRepository.findAll().stream()
            .filter(novel -> !interactedNovelIds.contains(novel.getId()))
            .toList();

        Map<Long, Double> contentScores = normalize(contentRecommendationService.scoreCandidates(interactions, candidates));
        Map<Long, Double> itemScores = normalize(itemSimilarityService.scoreCandidates(interactions, candidates));
        Map<Long, Double> semanticScores = normalize(semanticPreferenceRecommendationService.scoreCandidates(interactions));
        Map<Long, Double> popularityScores = normalize(popularityFreshnessScores(candidates));

        List<ScoredNovel> ranked = candidates.stream()
            .map(novel -> scoreNovel(novel, contentScores, itemScores, semanticScores, popularityScores))
            .sorted(Comparator.comparingDouble(ScoredNovel::score).reversed())
            .toList();

        return diversify(ranked, size).stream()
            .map(scored -> new RecommendationItemDTO(
                NovelMapper.toDTO(scored.novel()),
                scored.score(),
                scored.reason()
            ))
            .toList();
    }

    private ScoredNovel scoreNovel(
        Novel novel,
        Map<Long, Double> contentScores,
        Map<Long, Double> itemScores,
        Map<Long, Double> semanticScores,
        Map<Long, Double> popularityScores
    ) {
        double contentScore = contentScores.getOrDefault(novel.getId(), 0.0);
        double itemScore = itemScores.getOrDefault(novel.getId(), 0.0);
        double semanticScore = semanticScores.getOrDefault(novel.getId(), 0.0);
        double popularityScore = popularityScores.getOrDefault(novel.getId(), 0.0);
        boolean hasSemanticSignal = !semanticScores.isEmpty();
        double score = hasSemanticSignal
            ? SEMANTIC_CONTENT_WEIGHT * contentScore
                + SEMANTIC_ITEM_SIMILARITY_WEIGHT * itemScore
                + SEMANTIC_PREFERENCE_WEIGHT * semanticScore
                + SEMANTIC_POPULARITY_FRESHNESS_WEIGHT * popularityScore
            : CONTENT_WEIGHT * contentScore
                + ITEM_SIMILARITY_WEIGHT * itemScore
                + POPULARITY_FRESHNESS_WEIGHT * popularityScore;

        String reason = reason(contentScore, itemScore, semanticScore, popularityScore, hasSemanticSignal);
        return new ScoredNovel(novel, score, reason);
    }

    private String reason(
        double contentScore,
        double itemScore,
        double semanticScore,
        double popularityScore,
        boolean hasSemanticSignal
    ) {
        if (hasSemanticSignal && semanticScore >= contentScore && semanticScore >= itemScore && semanticScore > 0.0) {
            return "BASED_ON_SEMANTIC_PREFERENCE";
        }
        if (contentScore >= itemScore && contentScore >= popularityScore && contentScore > 0.0) {
            return "BASED_ON_CONTENT";
        }
        if (itemScore >= popularityScore && itemScore > 0.0) {
            return "BECAUSE_YOU_READ_SIMILAR";
        }
        return "TRENDING";
    }

    private Map<Long, Double> popularityFreshnessScores(List<Novel> candidates) {
        LocalDateTime now = LocalDateTime.now();
        Map<Long, Double> scores = new HashMap<>();
        for (Novel novel : candidates) {
            double popularity = Math.log1p(valueOrZero(novel.getViews()))
                + 3 * Math.log1p(valueOrZero(novel.getFollows()))
                + 4 * Math.log1p(valueOrZero(novel.getLikes()))
                + 3 * Math.log1p(valueOrZero(novel.getBookmarks()));
            long ageDays = novel.getUpdateAt() == null ? 365 : Math.max(0, Duration.between(novel.getUpdateAt(), now).toDays());
            double freshness = Math.exp(-ageDays / 30.0);
            scores.put(novel.getId(), popularity + freshness);
        }
        return scores;
    }

    private Map<Long, Double> normalize(Map<Long, Double> scores) {
        if (scores.isEmpty()) {
            return scores;
        }
        double min = scores.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        if (Double.compare(min, max) == 0) {
            return scores.keySet().stream().collect(java.util.stream.Collectors.toMap(id -> id, ignored -> 0.0));
        }

        Map<Long, Double> normalized = new HashMap<>();
        scores.forEach((novelId, score) -> normalized.put(novelId, (score - min) / (max - min)));
        return normalized;
    }

    private List<ScoredNovel> diversify(List<ScoredNovel> ranked, int size) {
        List<ScoredNovel> selected = new ArrayList<>();
        List<ScoredNovel> deferred = new ArrayList<>();
        Map<String, Integer> authorCounts = new HashMap<>();
        Map<String, Integer> genreCounts = new HashMap<>();

        for (ScoredNovel candidate : ranked) {
            if (selected.size() == size) {
                break;
            }
            if (canAdd(candidate.novel(), authorCounts, genreCounts)) {
                selected.add(candidate);
                incrementDiversityCounts(candidate.novel(), authorCounts, genreCounts);
            } else {
                deferred.add(candidate);
            }
        }
        for (ScoredNovel candidate : deferred) {
            if (selected.size() == size) {
                break;
            }
            selected.add(candidate);
        }
        return selected;
    }

    private boolean canAdd(Novel novel, Map<String, Integer> authorCounts, Map<String, Integer> genreCounts) {
        String author = novel.getAuthor() == null ? "" : novel.getAuthor().trim();
        String primaryGenre = primaryGenre(novel);
        return authorCounts.getOrDefault(author, 0) < MAX_PER_AUTHOR
            && genreCounts.getOrDefault(primaryGenre, 0) < MAX_PER_PRIMARY_GENRE;
    }

    private void incrementDiversityCounts(Novel novel, Map<String, Integer> authorCounts, Map<String, Integer> genreCounts) {
        String author = novel.getAuthor() == null ? "" : novel.getAuthor().trim();
        String primaryGenre = primaryGenre(novel);
        authorCounts.merge(author, 1, Integer::sum);
        genreCounts.merge(primaryGenre, 1, Integer::sum);
    }

    private String primaryGenre(Novel novel) {
        return novel.getGenres().stream().map(Genre::getName).sorted().findFirst().orElse("__none__");
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private record ScoredNovel(Novel novel, double score, String reason) {
    }
}
