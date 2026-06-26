package com.example.netnovel_server.embedding.service;

import com.example.netnovel_server.entity.Genre;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NovelEmbeddingTextBuilder {

    public String build(Novel novel) {
        return """
            Title: %s
            Author: %s
            Status: %s
            Genres: %s
            Tags: %s
            Description: %s
            """.formatted(
            clean(novel.getTitle()),
            clean(novel.getAuthor()),
            novel.getStatus() == null ? "" : novel.getStatus().name(),
            joinGenres(novel.getGenres()),
            joinTags(novel.getTags()),
            clean(novel.getDescription())
        ).trim();
    }

    private String joinGenres(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return "";
        }
        return genres.stream()
            .map(Genre::getName)
            .filter(value -> value != null && !value.isBlank())
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.joining(", "));
    }

    private String joinTags(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return tags.stream()
            .map(Tag::getName)
            .filter(value -> value != null && !value.isBlank())
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.joining(", "));
    }

    private String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
