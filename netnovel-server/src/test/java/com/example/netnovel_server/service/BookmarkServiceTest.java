package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.BookmarkCreateDTO;
import com.example.netnovel_server.dto.BookmarkDTO;
import com.example.netnovel_server.entity.AuthProvider;
import com.example.netnovel_server.entity.Bookmark;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Role;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.repository.BookmarkRepository;
import com.example.netnovel_server.repository.ChapterRepository;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    /*
     * Unit scope:
     * - BookmarkService branching for novel vs chapter bookmarks.
     * - SecurityUtils is statically mocked so no Spring Security context is required.
     * - Repositories are mocked; aggregate bookmark counters are verified by interaction.
     */

    private static final Long CURRENT_USER_ID = 7L;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NovelRepository novelRepository;

    @Mock
    private ChapterRepository chapterRepository;

    private BookmarkService bookmarkService;

    @BeforeEach
    void setUp() {
        bookmarkService = new BookmarkService(bookmarkRepository, userRepository, novelRepository, chapterRepository);
    }

    // Create flow: creating a bookmark requires a non-null request and exactly one target (novel or chapter).
    @Test
    void createBookmarkRejectsNullRequest() {
        assertThrows(BadRequestException.class, () -> bookmarkService.createBookmark(null));

        verify(bookmarkRepository, never()).save(any());
    }

    // Create flow: creating a bookmark requires exactly one target (novel or chapter).
    @Test
    void createBookmarkRequiresExactlyOneTarget() {
        assertThrows(BadRequestException.class, () -> bookmarkService.createBookmark(new BookmarkCreateDTO()));

        BookmarkCreateDTO bothTargets = BookmarkCreateDTO.builder()
            .novelId(10L)
            .chapterId(20L)
            .build();

        assertThrows(BadRequestException.class, () -> bookmarkService.createBookmark(bothTargets));
        verify(bookmarkRepository, never()).save(any());
    }

    // Create flow: creating a bookmark and saving it increments the parent novel's bookmark counter.
    @Test
    void createNovelBookmarkSavesBookmarkAndIncrementsNovelCounter() {
        User user = user(CURRENT_USER_ID);
        Novel novel = novel(10L);

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(user));
        when(novelRepository.findById(novel.getId())).thenReturn(Optional.of(novel));
        when(bookmarkRepository.existsByUserIdAndNovelId(CURRENT_USER_ID, novel.getId())).thenReturn(false);
        when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(invocation -> {
            Bookmark bookmark = invocation.getArgument(0);
            bookmark.setId(100L);
            return bookmark;
        });

        try (MockedStatic<SecurityUtils> security = currentUser()) {
            BookmarkDTO response = bookmarkService.createNovelBookmark(novel.getId());

            assertEquals(100L, response.getBookmarkId());
            assertEquals(CURRENT_USER_ID, response.getUserId());
            assertEquals(novel.getId(), response.getNovelId());
            assertNull(response.getChapterId());
        }

        ArgumentCaptor<Bookmark> bookmarkCaptor = ArgumentCaptor.forClass(Bookmark.class);
        verify(bookmarkRepository).save(bookmarkCaptor.capture());
        assertEquals(user, bookmarkCaptor.getValue().getUser());
        assertEquals(novel, bookmarkCaptor.getValue().getNovel());
        assertNull(bookmarkCaptor.getValue().getChapter());
        verify(novelRepository).incrementBookmarks(novel.getId());
    }

    // Create guards: creating a duplicate bookmark is rejected before any saving or counter incrementing.
    @Test
    void createNovelBookmarkRejectsDuplicateBookmark() {
        User user = user(CURRENT_USER_ID);
        Novel novel = novel(10L);

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(user));
        when(novelRepository.findById(novel.getId())).thenReturn(Optional.of(novel));
        when(bookmarkRepository.existsByUserIdAndNovelId(CURRENT_USER_ID, novel.getId())).thenReturn(true);

        try (MockedStatic<SecurityUtils> security = currentUser()) {
            assertThrows(DuplicateResourceException.class, () -> bookmarkService.createNovelBookmark(novel.getId()));
        }

        verify(bookmarkRepository, never()).save(any());
        verify(novelRepository, never()).incrementBookmarks(any());
    }

    // Create flow: creating a chapter bookmark saves the bookmark and increments the parent novel's bookmark counter.
    @Test
    void createChapterBookmarkSavesBookmarkAndIncrementsParentNovelCounter() {
        User user = user(CURRENT_USER_ID);
        Novel novel = novel(10L);
        Chapter chapter = chapter(20L, novel);

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(user));
        when(chapterRepository.findById(chapter.getId())).thenReturn(Optional.of(chapter));
        when(bookmarkRepository.existsByUserIdAndChapterId(CURRENT_USER_ID, chapter.getId())).thenReturn(false);
        when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(invocation -> {
            Bookmark bookmark = invocation.getArgument(0);
            bookmark.setId(101L);
            return bookmark;
        });

        try (MockedStatic<SecurityUtils> security = currentUser()) {
            BookmarkDTO response = bookmarkService.createChapterBookmark(chapter.getId());

            assertEquals(101L, response.getBookmarkId());
            assertEquals(chapter.getId(), response.getChapterId());
            assertEquals(novel.getId(), response.getNovelId());
        }

        verify(novelRepository).incrementBookmarks(novel.getId());
    }

    // Delete flow: deleting a novel bookmark removes the bookmark and decrements the parent novel's bookmark counter.
    @Test
    void deleteMyNovelBookmarkRemovesBookmarkAndDecrementsNovelCounter() {
        Novel novel = novel(10L);
        Bookmark bookmark = Bookmark.builder()
            .id(100L)
            .user(user(CURRENT_USER_ID))
            .novel(novel)
            .build();

        when(bookmarkRepository.existsByUserIdAndNovelId(CURRENT_USER_ID, novel.getId())).thenReturn(true);
        when(bookmarkRepository.findByUserIdAndNovelId(CURRENT_USER_ID, novel.getId())).thenReturn(Optional.of(bookmark));

        try (MockedStatic<SecurityUtils> security = currentUser()) {
            bookmarkService.deleteMyNovelBookmark(novel.getId());
        }

        verify(bookmarkRepository).delete(bookmark);
        verify(novelRepository).decrementBookmarks(novel.getId());
    }

    // Delete guards: deleting a non-existent bookmark is rejected before any deletion or counter decrementing.
    @Test
    void deleteMyChapterBookmarkRejectsMissingBookmark() {
        when(bookmarkRepository.existsByUserIdAndChapterId(CURRENT_USER_ID, 20L)).thenReturn(false);

        try (MockedStatic<SecurityUtils> security = currentUser()) {
            assertThrows(ResourceNotFoundException.class, () -> bookmarkService.deleteMyChapterBookmark(20L));
        }

        verify(bookmarkRepository, never()).delete(any());
        verify(novelRepository, never()).decrementBookmarks(any());
    }

    private static MockedStatic<SecurityUtils> currentUser() {
        MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class);
        security.when(SecurityUtils::getCurrentUserIdOrThrow).thenReturn(CURRENT_USER_ID);
        return security;
    }

    private static User user(Long id) {
        return User.builder()
            .id(id)
            .username("reader")
            .email("reader@example.com")
            .provider(AuthProvider.LOCAL)
            .roles(Set.of(Role.USER))
            .build();
    }

    private static Novel novel(Long id) {
        return Novel.builder()
            .id(id)
            .title("Bookmarked Novel")
            .author("Author")
            .build();
    }

    private static Chapter chapter(Long id, Novel novel) {
        return Chapter.builder()
            .id(id)
            .novel(novel)
            .title("Chapter One")
            .chapterNumber(1)
            .build();
    }
}
