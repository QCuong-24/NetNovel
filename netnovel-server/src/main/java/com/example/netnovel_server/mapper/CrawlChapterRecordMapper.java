package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.CrawlChapterRecordDTO;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.CrawlChapterRecord;
import com.example.netnovel_server.entity.Novel;

public final class CrawlChapterRecordMapper {

    private CrawlChapterRecordMapper() {
    }

    public static CrawlChapterRecordDTO toDTO(CrawlChapterRecord record) {
        if (record == null) {
            return null;
        }

        Novel novel = record.getNovel();
        Chapter chapter = record.getChapter();

        return CrawlChapterRecordDTO.builder()
            .id(record.getId())
            .sourceName(record.getSourceName())
            .sourceChapterUrl(record.getSourceChapterUrl())
            .novelId(novel == null ? null : novel.getId())
            .novelTitle(novel == null ? null : novel.getTitle())
            .chapterId(chapter == null ? null : chapter.getId())
            .chapterTitle(chapter == null ? null : chapter.getTitle())
            .chapterNumber(chapter == null ? null : chapter.getChapterNumber())
            .status(record.getStatus() == null ? null : record.getStatus().name())
            .errorMessage(record.getErrorMessage())
            .crawledAt(record.getCrawledAt())
            .build();
    }
}
