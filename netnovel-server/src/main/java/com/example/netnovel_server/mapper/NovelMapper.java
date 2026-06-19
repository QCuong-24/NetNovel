package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.NovelCreateDTO;
import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.entity.Genre;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.NovelChapterInfo;
import com.example.netnovel_server.entity.Status;
import com.example.netnovel_server.entity.Tag;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public final class NovelMapper {

    private NovelMapper() {
    }

    public static NovelDTO toDTO(Novel novel) {
        if (novel == null) {
            return null;
        }

        NovelChapterInfo chapterInfo = novel.getChapterInfo();

        return NovelDTO.builder()
            .novelId(novel.getId())
            .title(novel.getTitle())
            .author(novel.getAuthor())
            .description(novel.getDescription())
            .coverImageUrl(novel.getCoverImageUrl())
            .coverImagePublicId(novel.getCoverImagePublicId())
            .views(novel.getViews())
            .follows(novel.getFollows())
            .likes(novel.getLikes())
            .bookmarks(novel.getBookmarks())
            .genres(toGenreNames(novel.getGenres()))
            .status(novel.getStatus() != null ? novel.getStatus().name() : null)
            .chapterCount(chapterInfo != null ? chapterInfo.getChapterCount() : 0)
            .latestChapterId(chapterInfo != null && chapterInfo.getLatestChapter() != null
                ? chapterInfo.getLatestChapter().getId()
                : null)
            .latestChapterNumber(chapterInfo != null ? chapterInfo.getLatestChapterNumber() : null)
            .latestChapterTitle(chapterInfo != null ? chapterInfo.getLatestChapterTitle() : null)
            .latestChapterUpdatedAt(chapterInfo != null ? chapterInfo.getLatestChapterUpdatedAt() : null)
            .createAt(novel.getCreateAt())
            .updateAt(novel.getUpdateAt())
            .build();
    }

    public static Novel toEntity(NovelCreateDTO dto, Set<Genre> genres, Set<Tag> tags) {
        if (dto == null) {
            return null;
        }

        return Novel.builder()
            .title(dto.getTitle())
            .author(dto.getAuthor())
            .description(dto.getDescription())
            .coverImageUrl(dto.getCoverImageUrl())
            .coverImagePublicId(dto.getCoverImagePublicId())
            .genres(genres != null ? genres : Collections.emptySet())
            .tags(tags != null ? tags : Collections.emptySet())
            .status(parseStatus(dto.getStatus()))
            .build();
    }

    private static Set<String> toGenreNames(Set<Genre> genres) {
        if (genres == null) {
            return Collections.emptySet();
        }

        return genres.stream()
            .map(Genre::getName)
            .collect(Collectors.toSet());
    }

    private static Status parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        return Status.valueOf(status.trim().toUpperCase());
    }
}
