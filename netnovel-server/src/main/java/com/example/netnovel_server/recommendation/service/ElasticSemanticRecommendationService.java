package com.example.netnovel_server.recommendation.service;

import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.search.elastic.service.ElasticSemanticNovelSearchService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix = "app.search.elastic", name = "enabled", havingValue = "true")
public class ElasticSemanticRecommendationService {

    private final NovelRepository novelRepository;
    private final ElasticSemanticNovelSearchService semanticNovelSearchService;

    public ElasticSemanticRecommendationService(
        NovelRepository novelRepository,
        ElasticSemanticNovelSearchService semanticNovelSearchService
    ) {
        this.novelRepository = novelRepository;
        this.semanticNovelSearchService = semanticNovelSearchService;
    }

    @Transactional(readOnly = true)
    public Page<NovelSearchResultDTO> getSimilarNovels(Long novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel not found");
        }
        return semanticNovelSearchService.similarNovels(novelId, pageable);
    }
}
