package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.NovelCreateDTO;
import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.entity.Novel;
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

        return NovelDTO.builder()
            .novelId(novel.getId())
            .title(novel.getTitle())
            .author(novel.getAuthor())
            .description(novel.getDescription())
            .coverImageUrl(novel.getCoverImageUrl())
            .views(novel.getViews())
            .follows(novel.getFollows())
            .likes(novel.getLikes())
            .tags(toTagNames(novel.getTags()))
            .status(novel.getStatus() != null ? novel.getStatus().name() : null)
            .createAt(novel.getCreateAt())
            .updateAt(novel.getUpdateAt())
            .build();
    }

    public static Novel toEntity(NovelCreateDTO dto, Set<Tag> tags) {
        if (dto == null) {
            return null;
        }

        return Novel.builder()
            .title(dto.getTitle())
            .author(dto.getAuthor())
            .description(dto.getDescription())
            .coverImageUrl(dto.getCoverImageUrl())
            .tags(tags != null ? tags : Collections.emptySet())
            .status(parseStatus(dto.getStatus()))
            .build();
    }

    private static Set<String> toTagNames(Set<Tag> tags) {
        if (tags == null) {
            return Collections.emptySet();
        }

        return tags.stream()
            .map(Tag::getName)
            .collect(Collectors.toSet());
    }

    private static Status parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        return Status.valueOf(status.trim().toUpperCase());
    }
}
