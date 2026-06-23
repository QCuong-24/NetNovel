package com.example.netnovel_server.recommendation.mapper;

import com.example.netnovel_server.recommendation.dto.UserNovelInteractionDTO;
import com.example.netnovel_server.recommendation.entity.UserNovelInteraction;

public final class UserNovelInteractionMapper {

    private UserNovelInteractionMapper() {
    }

    public static UserNovelInteractionDTO toDTO(UserNovelInteraction interaction) {
        return new UserNovelInteractionDTO(
            interaction.getUser().getId(),
            interaction.getNovel().getId(),
            interaction.getViewNovelCount(),
            interaction.getViewChapterCount(),
            interaction.getCommentCount(),
            interaction.getReplyCount(),
            Boolean.TRUE.equals(interaction.getFollowed()),
            Boolean.TRUE.equals(interaction.getLiked()),
            Boolean.TRUE.equals(interaction.getBookmarked()),
            interaction.getInteractionScore(),
            interaction.getFirstInteractedAt(),
            interaction.getLastInteractedAt(),
            interaction.getCalculatedAt()
        );
    }
}
