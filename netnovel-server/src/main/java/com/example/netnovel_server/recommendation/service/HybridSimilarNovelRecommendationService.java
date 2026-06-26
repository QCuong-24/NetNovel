package com.example.netnovel_server.recommendation.service;

import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.entity.Genre;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Tag;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.recommendation.dto.SimilarNovelRecommendationDTO;
import com.example.netnovel_server.repository.NovelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HybridSimilarNovelRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(HybridSimilarNovelRecommendationService.class);

    private static final double SEMANTIC_WEIGHT = 0.50;
    private static final double CONTENT_WEIGHT = 0.30;
    private static final double POPULARITY_WEIGHT = 0.20;

    private final NovelRepository novelRepository;
    private final RecommendationService recommendationService;
    private final ObjectProvider<ElasticSemanticRecommendationService> semanticRecommendationServiceProvider;

    public HybridSimilarNovelRecommendationService(
        NovelRepository novelRepository,
        RecommendationService recommendationService,
        ObjectProvider<ElasticSemanticRecommendationService> semanticRecommendationServiceProvider
    ) {
        this.novelRepository = novelRepository;
        this.recommendationService = recommendationService;
        this.semanticRecommendationServiceProvider = semanticRecommendationServiceProvider;
    }

    @Transactional(readOnly = true)
    public Page<SimilarNovelRecommendationDTO> getSimilarNovels(Long novelId, Pageable pageable) {
        Novel sourceNovel = novelRepository.findById(novelId)
            .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));

        int requestedEnd = Math.toIntExact(pageable.getOffset()) + Math.max(1, pageable.getPageSize());
        int candidateSize = Math.max(30, requestedEnd * 3);
        Pageable candidatePageable = PageRequest.of(0, candidateSize);

        Map<Long, Double> contentScores = scoresByNovelId(recommendationService.getSimilarNovels(novelId, candidatePageable));
        Map<Long, Double> semanticScores = semanticScores(novelId, candidatePageable);

        Set<Long> candidateIds = new LinkedHashSet<>();
        candidateIds.addAll(contentScores.keySet());
        candidateIds.addAll(semanticScores.keySet());
        candidateIds.remove(novelId);

        Map<Long, Novel> novelsById = novelRepository.findAllById(candidateIds).stream()
            .collect(Collectors.toMap(Novel::getId, Function.identity()));
        Map<Long, Double> normalizedContent = normalize(contentScores);
        Map<Long, Double> normalizedSemantic = normalize(semanticScores);
        Map<Long, Double> normalizedPopularity = normalize(popularityScores(novelsById.values()));

        List<SimilarNovelRecommendationDTO> ranked = candidateIds.stream()
            .map(candidateId -> toRecommendation(
                sourceNovel,
                novelsById.get(candidateId),
                normalizedSemantic.getOrDefault(candidateId, 0.0),
                normalizedContent.getOrDefault(candidateId, 0.0),
                normalizedPopularity.getOrDefault(candidateId, 0.0)
            ))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingDouble(SimilarNovelRecommendationDTO::getScore).reversed())
            .toList();

        int from = Math.min(Math.toIntExact(pageable.getOffset()), ranked.size());
        int to = Math.min(from + pageable.getPageSize(), ranked.size());
        return new PageImpl<>(ranked.subList(from, to), pageable, ranked.size());
    }

    private Map<Long, Double> semanticScores(Long novelId, Pageable pageable) {
        ElasticSemanticRecommendationService service = semanticRecommendationServiceProvider.getIfAvailable();
        if (service == null) {
            return Map.of();
        }
        try {
            return scoresByNovelId(service.getSimilarNovels(novelId, pageable));
        } catch (Exception exception) {
            log.warn("Semantic similar novels failed; hybrid recommendation will use content-based fallback", exception);
            return Map.of();
        }
    }

    private Map<Long, Double> scoresByNovelId(Page<NovelSearchResultDTO> page) {
        Map<Long, Double> scores = new LinkedHashMap<>();
        for (NovelSearchResultDTO result : page.getContent()) {
            NovelDTO novel = result.getNovel();
            if (novel == null || novel.getNovelId() == null) {
                continue;
            }
            scores.put(novel.getNovelId(), result.getScore() == null ? 0.0 : result.getScore());
        }
        return scores;
    }

    private SimilarNovelRecommendationDTO toRecommendation(
        Novel sourceNovel,
        Novel candidate,
        double semanticScore,
        double contentScore,
        double popularityScore
    ) {
        if (candidate == null) {
            return null;
        }

        double score = SEMANTIC_WEIGHT * semanticScore
            + CONTENT_WEIGHT * contentScore
            + POPULARITY_WEIGHT * popularityScore;

        return SimilarNovelRecommendationDTO.builder()
            .novel(NovelMapper.toDTO(candidate))
            .score(score)
            .semanticScore(semanticScore)
            .contentScore(contentScore)
            .popularityScore(popularityScore)
            .reasons(reasons(sourceNovel, candidate, semanticScore, contentScore, popularityScore))
            .build();
    }

    private List<String> reasons(
        Novel sourceNovel,
        Novel candidate,
        double semanticScore,
        double contentScore,
        double popularityScore
    ) {
        List<String> reasons = new ArrayList<>();
        if (semanticScore > 0.0 && semanticScore >= contentScore) {
            reasons.add("Nội dung/vibe tương tự truyện gốc");
        }
        if (contentScore > 0.0) {
            reasons.add("Có tín hiệu nội dung và metadata giống truyện gốc");
        }

        List<String> sharedGenres = sharedNames(
            sourceNovel.getGenres(),
            candidate.getGenres(),
            Genre::getName
        );
        if (!sharedGenres.isEmpty()) {
            reasons.add("Cùng genre: " + String.join(", ", limit(sharedGenres, 3)));
        }

        List<String> sharedTags = sharedNames(
            sourceNovel.getTags(),
            candidate.getTags(),
            Tag::getName
        );
        if (!sharedTags.isEmpty()) {
            reasons.add("Có tag giống: " + String.join(", ", limit(sharedTags, 3)));
        }

        if (popularityScore >= 0.65) {
            reasons.add("Được nhiều người quan tâm");
        }
        if (reasons.isEmpty()) {
            reasons.add("Gợi ý cân bằng từ nội dung, semantic và độ phổ biến");
        }
        return reasons;
    }

    private <T> List<String> sharedNames(Set<T> left, Set<T> right, Function<T, String> nameExtractor) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            return List.of();
        }
        Set<String> rightNames = right.stream()
            .map(nameExtractor)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        return left.stream()
            .map(nameExtractor)
            .filter(Objects::nonNull)
            .filter(rightNames::contains)
            .sorted()
            .toList();
    }

    private List<String> limit(List<String> values, int limit) {
        return values.size() <= limit ? values : values.subList(0, limit);
    }

    private Map<Long, Double> popularityScores(Collection<Novel> novels) {
        Map<Long, Double> scores = new HashMap<>();
        for (Novel novel : novels) {
            double score = Math.log1p(safeLong(novel.getViews()))
                + 2 * Math.log1p(safeLong(novel.getLikes()))
                + 3 * Math.log1p(safeLong(novel.getFollows()))
                + 3 * Math.log1p(safeLong(novel.getBookmarks()));
            scores.put(novel.getId(), score);
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
            return scores.keySet().stream().collect(Collectors.toMap(Function.identity(), ignored -> 1.0));
        }

        Map<Long, Double> normalized = new HashMap<>();
        scores.forEach((novelId, score) -> normalized.put(novelId, (score - min) / (max - min)));
        return normalized;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
