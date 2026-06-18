package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.BookmarkDTO;
import com.example.netnovel_server.entity.Bookmark;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.User;

public final class BookmarkMapper {

    private BookmarkMapper() {
    }

    public static BookmarkDTO toDTO(Bookmark bookmark) {
        if (bookmark == null) {
            return null;
        }

        User user = bookmark.getUser();
        Novel bookmarkedNovel = bookmark.getNovel();
        Chapter chapter = bookmark.getChapter();
        Novel chapterNovel = chapter != null ? chapter.getNovel() : null;
        Novel novel = bookmarkedNovel != null ? bookmarkedNovel : chapterNovel;

        return BookmarkDTO.builder()
            .bookmarkId(bookmark.getId())
            .userId(user != null ? user.getId() : null)
            .novelId(novel != null ? novel.getId() : null)
            .novelTitle(novel != null ? novel.getTitle() : null)
            .author(novel != null ? novel.getAuthor() : null)
            .coverImageUrl(novel != null ? novel.getCoverImageUrl() : null)
            .chapterId(chapter != null ? chapter.getId() : null)
            .chapterTitle(chapter != null ? chapter.getTitle() : null)
            .chapterNumber(chapter != null ? chapter.getChapterNumber() : null)
            .createdAt(bookmark.getCreatedAt())
            .build();
    }
}
