package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.repository.NovelSearchProjection;
import com.example.netnovel_server.repository.NovelSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final NovelRepository novelRepository;
    private final NovelSearchRepository novelSearchRepository;

    public RecommendationService(NovelRepository novelRepository, NovelSearchRepository novelSearchRepository) {
        this.novelRepository = novelRepository;
        this.novelSearchRepository = novelSearchRepository;
    }

    @Transactional(readOnly = true)
    public Page<NovelSearchResultDTO> getSimilarNovels(Long novelId, Pageable pageable) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel not found");
        }

        Page<NovelSearchProjection> resultPage = novelSearchRepository.findSimilarNovels(novelId, pageable);
        List<Long> novelIds = resultPage.getContent().stream()
            .map(NovelSearchProjection::getNovelId)
            .toList();
        Map<Long, Novel> novelsById = novelRepository.findAllById(novelIds).stream()
            .collect(Collectors.toMap(Novel::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        List<NovelSearchResultDTO> results = resultPage.getContent().stream()
            .map(projection -> NovelSearchResultDTO.builder()
                .novel(NovelMapper.toDTO(novelsById.get(projection.getNovelId())))
                .score(projection.getScore())
                .build())
            .filter(result -> result.getNovel() != null)
            .toList();

        return new PageImpl<>(results, pageable, resultPage.getTotalElements());
    }
}
