package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.ChapterContentDTO;
import com.example.netnovel_server.dto.ChapterCreateDTO;
import com.example.netnovel_server.dto.ChapterDTO;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Novel;

public final class ChapterMapper {

    private ChapterMapper() {
    }

    public static ChapterDTO toDTO(Chapter chapter) {
        if (chapter == null) {
            return null;
        }

        Novel novel = chapter.getNovel();

        return ChapterDTO.builder()
            .chapterId(chapter.getId())
            .novelId(novel != null ? novel.getId() : null)
            .novelTitle(novel != null ? novel.getTitle() : null)
            .title(chapter.getTitle())
            .chapterNumber(chapter.getChapterNumber())
            .updateAt(chapter.getUpdateAt())
            .build();
    }

    public static ChapterContentDTO toContentDTO(Chapter chapter) {
        if (chapter == null) {
            return null;
        }

        Novel novel = chapter.getNovel();

        return ChapterContentDTO.builder()
            .chapterId(chapter.getId())
            .novelId(novel != null ? novel.getId() : null)
            .novelTitle(novel != null ? novel.getTitle() : null)
            .title(chapter.getTitle())
            .chapterNumber(chapter.getChapterNumber())
            .content(chapter.getContent() != null ? chapter.getContent().getContent() : null)
            .updateAt(chapter.getUpdateAt())
            .build();
    }

    public static Chapter toEntity(ChapterCreateDTO dto, Novel novel) {
        if (dto == null) {
            return null;
        }

        Chapter chapter = Chapter.builder()
            .novel(novel)
            .title(dto.getTitle())
            .chapterNumber(dto.getChapterNumber())
            .build();
        chapter.setContent(dto.getContent());
        return chapter;
    }

}
