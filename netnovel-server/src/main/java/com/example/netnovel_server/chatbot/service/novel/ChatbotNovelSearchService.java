package com.example.netnovel_server.chatbot.service.novel;

import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.entity.Genre;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Status;
import com.example.netnovel_server.entity.Tag;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.chatbot.service.language.ChatbotTextNormalizer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ChatbotNovelSearchService {

    private static final int DEFAULT_LIMIT = 3;

    private final NovelRepository novelRepository;
    private final ChatbotTextNormalizer normalizer;

    public ChatbotNovelSearchService(NovelRepository novelRepository, ChatbotTextNormalizer normalizer) {
        this.novelRepository = novelRepository;
        this.normalizer = normalizer;
    }

    @Transactional(readOnly = true)
    public List<NovelDTO> search(Map<String, String> filters) {
        String sort = filters.getOrDefault("sort", "relevance");

        List<Novel> novels = switch (sort) {
            case "popular" -> novelRepository.findAll(PageRequest.of(0, 30, Sort.by(Sort.Direction.DESC, "views"))).getContent();
            case "latest" -> novelRepository.findAllByOrderByUpdateAtDesc(PageRequest.of(0, 30)).getContent();
            default -> novelRepository.findAll(PageRequest.of(0, 80, Sort.by(Sort.Direction.DESC, "updateAt"))).getContent();
        };

        String status = filters.get("status");
        String genre = filters.get("genre");
        String tag = filters.get("tag");
        String author = normalizer.normalize(filters.get("author"));
        String query = normalizer.normalize(filters.get("q"));
        String scope = filters.get("scope");

        return novels.stream()
            .filter(novel -> matchesStatus(novel, status))
            .filter(novel -> matchesGenre(novel, genre))
            .filter(novel -> matchesTag(novel, tag))
            .filter(novel -> matchesAuthor(novel, author))
            .filter(novel -> matchesQuery(novel, query, scope))
            .sorted(sortComparator(sort))
            .limit(DEFAULT_LIMIT)
            .map(NovelMapper::toDTO)
            .toList();
    }

    private boolean matchesStatus(Novel novel, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }

        try {
            return novel.getStatus() == Status.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return true;
        }
    }

    private boolean matchesGenre(Novel novel, String genre) {
        if (genre == null || genre.isBlank()) {
            return true;
        }

        String normalizedGenre = normalizer.normalize(genre);
        return novel.getGenres() != null && novel.getGenres().stream()
            .map(Genre::getName)
            .map(normalizer::normalize)
            .anyMatch(name -> name.equals(normalizedGenre) || name.contains(normalizedGenre));
    }

    private boolean matchesTag(Novel novel, String tag) {
        if (tag == null || tag.isBlank()) {
            return true;
        }

        String normalizedTag = normalizer.normalize(tag);
        return novel.getTags() != null && novel.getTags().stream()
            .map(Tag::getName)
            .map(normalizer::normalize)
            .anyMatch(name -> name.equals(normalizedTag) || name.contains(normalizedTag));
    }

    private boolean matchesAuthor(Novel novel, String author) {
        if (author == null || author.isBlank()) {
            return true;
        }

        return normalizer.normalize(novel.getAuthor()).contains(author);
    }

    private boolean matchesQuery(Novel novel, String query, String scope) {
        if (query == null || query.isBlank()) {
            return true;
        }

        if ("title".equals(scope)) {
            return normalizer.normalize(novel.getTitle()).contains(query);
        }

        String haystack = normalizer.normalize(String.join(" ",
            safe(novel.getTitle()),
            safe(novel.getAuthor()),
            safe(novel.getDescription())
        ));

        return haystack.contains(query);
    }

    private Comparator<Novel> sortComparator(String sort) {
        if ("popular".equals(sort)) {
            return Comparator.comparingLong((Novel novel) -> safeLong(novel.getViews())).reversed();
        }
        if ("latest".equals(sort)) {
            return Comparator.comparing(Novel::getUpdateAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        }
        return Comparator.comparing(Novel::getUpdateAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}

