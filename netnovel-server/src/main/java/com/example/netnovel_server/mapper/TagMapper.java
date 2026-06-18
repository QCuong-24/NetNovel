package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.TagDTO;
import com.example.netnovel_server.entity.Tag;

public final class TagMapper {

    private TagMapper() {
    }

    public static TagDTO toDTO(Tag tag) {
        if (tag == null) {
            return null;
        }

        return TagDTO.builder()
            .tagId(tag.getId())
            .name(tag.getName())
            .build();
    }

    public static Tag toEntity(TagDTO dto) {
        if (dto == null) {
            return null;
        }

        return Tag.builder()
            .id(dto.getTagId())
            .name(dto.getName())
            .build();
    }
}
