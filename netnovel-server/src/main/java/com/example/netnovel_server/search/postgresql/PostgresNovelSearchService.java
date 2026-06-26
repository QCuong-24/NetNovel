package com.example.netnovel_server.search.postgresql;

import com.example.netnovel_server.dto.NovelSearchResultDTO;
import com.example.netnovel_server.dto.SearchSuggestionDTO;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Status;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.utility.TextUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PostgresNovelSearchService {

    private static final int DEFAULT_SUGGESTION_LIMIT = 8;
    private static final int MAX_SUGGESTION_LIMIT = 20;

    private final NovelSearchRepository novelSearchRepository;
    private final NovelRepository novelRepository;

    public PostgresNovelSearchService(
        NovelSearchRepository novelSearchRepository,
        NovelRepository novelRepository
    ) {
        this.novelSearchRepository = novelSearchRepository;
        this.novelRepository = novelRepository;
    }

    @Transactional(readOnly = true)
    public Page<NovelSearchResultDTO> searchNovels(
        String query,
        String status,
        List<String> genres,
        String sort,
        Pageable pageable
    ) {
        String normalizedQuery = normalize(query);
        String normalizedStatus = normalizeStatus(status);
        List<String> normalizedGenres = normalizeCatalogNames(genres);
        String normalizedSort = normalizeSort(sort);

        Page<NovelSearchProjection> resultPage = novelSearchRepository.searchNovels(
            normalizedQuery,
            normalizedStatus,
            normalizedGenres.isEmpty() ? List.of("__no_genre_filter__") : normalizedGenres,
            normalizedGenres.size(),
            normalizedSort,
            pageable
        );

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

    @Transactional(readOnly = true)
    public List<SearchSuggestionDTO> suggest(String query, Integer limit) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }

        int normalizedLimit = normalizeLimit(limit);
        return novelSearchRepository.findSuggestions(normalizedQuery, normalizedLimit).stream()
            .map(this::toSuggestionDTO)
            .toList();
    }

    private SearchSuggestionDTO toSuggestionDTO(SearchSuggestionProjection projection) {
        return SearchSuggestionDTO.builder()
            .type(projection.getType())
            .id(projection.getId())
            .label(projection.getLabel())
            .build();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeStatus(String status) {
        String normalized = normalize(status);
        if (normalized.isBlank()) {
            return "";
        }

        try {
            return Status.valueOf(normalized.toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid novel status: " + status);
        }
    }

    private String normalizeCatalogName(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return "";
        }
        return TextUtils.toTitleCaseWords(normalized);
    }

    private List<String> normalizeCatalogNames(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
            .filter(Objects::nonNull)
            .flatMap(value -> Arrays.stream(value.split(",")))
            .map(this::normalizeCatalogName)
            .filter(value -> !value.isBlank())
            .map(value -> value.toLowerCase(Locale.ROOT))
            .distinct()
            .toList();
    }

    private String normalizeSort(String sort) {
        String normalized = normalize(sort).toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "relevance";
        }
        if (!normalized.equals("relevance") && !normalized.equals("latest") && !normalized.equals("popular")) {
            throw new BadRequestException("Invalid search sort: " + sort);
        }
        return normalized;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_SUGGESTION_LIMIT;
        }
        if (limit < 1) {
            return 1;
        }
        return Math.min(limit, MAX_SUGGESTION_LIMIT);
    }
}
