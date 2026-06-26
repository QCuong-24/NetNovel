package com.example.netnovel_server.search.elastic.mapper;

import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.NovelChapterInfo;
import com.example.netnovel_server.entity.NovelSource;
import com.example.netnovel_server.entity.Genre;
import com.example.netnovel_server.entity.Tag;
import com.example.netnovel_server.repository.NovelSourceRepository;
import com.example.netnovel_server.search.elastic.document.ElasticNovelDocument;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class NovelSearchDocumentMapper {

    private final NovelSourceRepository novelSourceRepository;

    public NovelSearchDocumentMapper(NovelSourceRepository novelSourceRepository) {
        this.novelSourceRepository = novelSourceRepository;
    }

    public ElasticNovelDocument toDocument(Novel novel) {
        NovelChapterInfo chapterInfo = novel.getChapterInfo();
        NovelSource source = novelSourceRepository.findFirstByNovelIdOrderByLastCrawledAtDesc(novel.getId())
            .orElse(null);
        Set<String> genreNames = novel.getGenres() == null
            ? Set.of()
            : novel.getGenres().stream().map(Genre::getName).collect(Collectors.toSet());
        Set<String> tagNames = novel.getTags() == null
            ? Set.of()
            : novel.getTags().stream().map(Tag::getName).collect(Collectors.toSet());

        return ElasticNovelDocument.builder()
            .novelId(novel.getId())
            .title(novel.getTitle())
            .author(novel.getAuthor())
            .description(novel.getDescription())
            .genres(genreNames)
            .tags(tagNames)
            .status(novel.getStatus() == null ? null : novel.getStatus().name())
            .views(novel.getViews())
            .follows(novel.getFollows())
            .likes(novel.getLikes())
            .bookmarks(novel.getBookmarks())
            .chapterCount(chapterInfo == null ? 0 : chapterInfo.getChapterCount())
            .latestChapterNumber(chapterInfo == null ? null : chapterInfo.getLatestChapterNumber())
            .lastChapterUpdatedAt(chapterInfo == null ? null : chapterInfo.getLatestChapterUpdatedAt())
            .createdAt(novel.getCreateAt())
            .updatedAt(novel.getUpdateAt())
            .crawled(source != null)
            .sourceName(source == null ? null : source.getSourceName())
            .sourceNovelUrl(source == null ? null : source.getSourceNovelUrl())
            .popularityScore(popularityScore(novel))
            .freshnessScore(freshnessScore(novel, chapterInfo))
            .recommendationText(recommendationText(novel, genreNames, tagNames))
            .build();
    }

    private double popularityScore(Novel novel) {
        return Math.log1p(safeLong(novel.getViews()))
            + Math.log1p(safeLong(novel.getLikes())) * 2
            + Math.log1p(safeLong(novel.getFollows())) * 3;
    }

    private double freshnessScore(Novel novel, NovelChapterInfo chapterInfo) {
        LocalDateTime baseTime = chapterInfo != null && chapterInfo.getLatestChapterUpdatedAt() != null
            ? chapterInfo.getLatestChapterUpdatedAt()
            : novel.getUpdateAt();
        if (baseTime == null) {
            return 0;
        }

        long days = Math.max(0, Duration.between(baseTime, LocalDateTime.now()).toDays());
        return 1.0 / (1 + days);
    }

    private String recommendationText(Novel novel, Set<String> genres, Set<String> tags) {
        return Stream.of(
                novel.getTitle(),
                novel.getAuthor(),
                String.join(" ", genres),
                String.join(" ", tags),
                novel.getDescription()
            )
            .filter(value -> value != null && !value.isBlank())
            .collect(Collectors.joining(" "));
    }

    private long safeLong(Long value) {
        return value == null ? 0 : value;
    }
}
