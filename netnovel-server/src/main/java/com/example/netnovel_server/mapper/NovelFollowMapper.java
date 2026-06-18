package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.NovelFollowDTO;
import com.example.netnovel_server.entity.NovelFollow;
import com.example.netnovel_server.entity.User;

public final class NovelFollowMapper {

    private NovelFollowMapper() {
    }

    public static NovelFollowDTO toDTO(NovelFollow follow) {
        if (follow == null) {
            return null;
        }

        User user = follow.getUser();

        return NovelFollowDTO.builder()
            .followId(follow.getId())
            .userId(user != null ? user.getId() : null)
            .novel(NovelMapper.toDTO(follow.getNovel()))
            .followedAt(follow.getFollowedAt())
            .build();
    }
}
