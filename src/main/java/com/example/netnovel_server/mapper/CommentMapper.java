package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.CommentDTO;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Comment;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.User;

public final class CommentMapper {

    private CommentMapper() {
    }

    public static CommentDTO toDTO(Comment comment) {
        if (comment == null) {
            return null;
        }

        Chapter chapter = comment.getChapter();
        Novel novel = comment.getNovel();
        User user = comment.getUser();

        return CommentDTO.builder()
            .commentId(comment.getId())
            .novelId(novel != null ? novel.getId() : null)
            .chapterId(chapter != null ? chapter.getId() : null)
            .chapterNumber(chapter != null ? chapter.getChapterNumber() : null)
            .userId(user != null ? user.getId() : null)
            .username(user != null ? user.getUsername() : null)
            .userAvatarUrl(user != null ? user.getProfilePictureUrl() : null)
            .content(comment.getContent())
            .deleted(comment.getDeleted())
            .createdAt(comment.getCreatedAt())
            .lastActivityAt(comment.getLastActivityAt())
            .build();
    }

    public static Comment toEntity(
        String content,
        User user,
        Novel novel,
        Chapter chapter,
        Comment parentComment,
        Comment rootComment
    ) {
        return Comment.builder()
            .user(user)
            .novel(novel)
            .chapter(chapter)
            .parentComment(parentComment)
            .rootComment(rootComment)
            .content(content)
            .build();
    }

}
