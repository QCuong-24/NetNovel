package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.NovelLastReadDTO;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.NovelLastRead;
import com.example.netnovel_server.entity.User;

public final class NovelLastReadMapper {

    private NovelLastReadMapper() {
    }

    public static NovelLastReadDTO toDTO(NovelLastRead lastRead) {
        if (lastRead == null) {
            return null;
        }

        User user = lastRead.getUser();
        Novel novel = lastRead.getNovel();
        Chapter chapter = lastRead.getChapter();

        return NovelLastReadDTO.builder()
            .lastReadId(lastRead.getId())
            .userId(user != null ? user.getId() : null)
            .novelId(novel != null ? novel.getId() : null)
            .novelTitle(novel != null ? novel.getTitle() : null)
            .author(novel != null ? novel.getAuthor() : null)
            .coverImageUrl(novel != null ? novel.getCoverImageUrl() : null)
            .chapterId(chapter != null ? chapter.getId() : null)
            .chapterTitle(chapter != null ? chapter.getTitle() : null)
            .chapterNumber(chapter != null ? chapter.getChapterNumber() : null)
            .lastReadAt(lastRead.getLastReadAt())
            .build();
    }

    public static NovelLastRead toEntity(User user, Novel novel, Chapter chapter) {
        return NovelLastRead.builder()
            .user(user)
            .novel(novel)
            .chapter(chapter)
            .build();
    }

}
