package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.ChapterContentDTO;
import com.example.netnovel_server.dto.ChapterCreateDTO;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.repository.BookmarkRepository;
import com.example.netnovel_server.repository.ChapterRepository;
import com.example.netnovel_server.repository.NovelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChapterServiceTest {

    /*
     * Unit scope:
     * - ChapterService create/update/delete behavior.
     * - Repositories and NovelChapterInfoService are mocked.
     * - Preview-only read behavior is left for separate security/context tests.
     */

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private NovelRepository novelRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private NovelChapterInfoService novelChapterInfoService;

    private ChapterService chapterService;

    @BeforeEach
    void setUp() {
        chapterService = new ChapterService(
            chapterRepository,
            novelRepository,
            bookmarkRepository,
            novelChapterInfoService
        );
    }

    // Create flow: a new chapter is saved and the novel's updateAt timestamp is refreshed.
    @Test
    void createChapterSavesChapterAndRefreshesNovelInfo() {
        Long novelId = 10L;
        Novel novel = novel(novelId);
        ChapterCreateDTO request = request("The First Door", 1, "This is a long enough chapter content.");
        Chapter savedChapter = chapter(100L, novel, request.getTitle(), request.getChapterNumber(), request.getContent());

        when(novelRepository.findById(novelId)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumber(novelId, request.getChapterNumber())).thenReturn(false);
        when(chapterRepository.save(any(Chapter.class))).thenReturn(savedChapter);

        ChapterContentDTO response = chapterService.createChapter(novelId, request);

        assertEquals(savedChapter.getId(), response.getChapterId());
        assertEquals(novelId, response.getNovelId());
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(novel.getTitle(), response.getNovelTitle());
        assertEquals(request.getChapterNumber(), response.getChapterNumber());
        assertEquals(request.getContent(), response.getContent());

        ArgumentCaptor<Chapter> chapterCaptor = ArgumentCaptor.forClass(Chapter.class);
        verify(chapterRepository).save(chapterCaptor.capture());
        Chapter chapterToSave = chapterCaptor.getValue();
        assertEquals(novel, chapterToSave.getNovel());
        assertEquals(request.getTitle(), chapterToSave.getTitle());
        assertEquals(request.getChapterNumber(), chapterToSave.getChapterNumber());
        assertEquals(request.getContent(), chapterToSave.getContent().getContent());

        verify(novelRepository).advanceUpdateAt(novelId, savedChapter.getUpdateAt());
        verify(novelChapterInfoService).refresh(novelId);
    }

    // Create guards: no chapter should be saved when the owning novel is missing.
    @Test
    void createChapterRejectsMissingNovel() {
        Long novelId = 10L;
        ChapterCreateDTO request = request("The First Door", 1, "This is a long enough chapter content.");

        when(novelRepository.findById(novelId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chapterService.createChapter(novelId, request));

        verify(chapterRepository, never()).save(any());
        verify(novelChapterInfoService, never()).refresh(any());
    }

    // Create guards: chapter numbers must stay unique within a novel.
    @Test
    void createChapterRejectsDuplicateChapterNumber() {
        Long novelId = 10L;
        Novel novel = novel(novelId);
        ChapterCreateDTO request = request("The First Door", 1, "This is a long enough chapter content.");

        when(novelRepository.findById(novelId)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumber(novelId, request.getChapterNumber())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> chapterService.createChapter(novelId, request));

        verify(chapterRepository, never()).save(any());
        verify(novelChapterInfoService, never()).refresh(any());
    }

    // Update flow: mutable fields are copied onto the existing entity before save.
    @Test
    void updateChapterChangesMutableFieldsAndRefreshesNovelInfo() {
        Novel novel = novel(10L);
        Chapter existingChapter = chapter(100L, novel, "Old Title", 1, "Old chapter content is long enough.");
        ChapterCreateDTO request = request("New Title", 2, "New chapter content is also long enough.");
        Chapter savedChapter = chapter(existingChapter.getId(), novel, request.getTitle(), request.getChapterNumber(), request.getContent());

        when(chapterRepository.findById(existingChapter.getId())).thenReturn(Optional.of(existingChapter));
        when(chapterRepository.existsByNovelIdAndChapterNumber(novel.getId(), request.getChapterNumber())).thenReturn(false);
        when(chapterRepository.saveAndFlush(existingChapter)).thenReturn(savedChapter);

        ChapterContentDTO response = chapterService.updateChapter(existingChapter.getId(), request);

        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getChapterNumber(), response.getChapterNumber());
        assertEquals(request.getContent(), response.getContent());
        assertEquals(request.getTitle(), existingChapter.getTitle());
        assertEquals(request.getChapterNumber(), existingChapter.getChapterNumber());
        assertEquals(request.getContent(), existingChapter.getContent().getContent());

        verify(novelRepository).advanceUpdateAt(novel.getId(), savedChapter.getUpdateAt());
        verify(novelChapterInfoService).refresh(novel.getId());
    }

    // Update guards: chapter numbers must stay unique within a novel, unless the number is unchanged.
    @Test
    void updateChapterAllowsKeepingSameChapterNumber() {
        Novel novel = novel(10L);
        Chapter existingChapter = chapter(100L, novel, "Old Title", 1, "Old chapter content is long enough.");
        ChapterCreateDTO request = request("New Title", 1, "New chapter content is also long enough.");

        when(chapterRepository.findById(existingChapter.getId())).thenReturn(Optional.of(existingChapter));
        when(chapterRepository.saveAndFlush(existingChapter)).thenReturn(existingChapter);

        chapterService.updateChapter(existingChapter.getId(), request);

        verify(chapterRepository, never()).existsByNovelIdAndChapterNumber(any(), any());
        verify(chapterRepository).saveAndFlush(existingChapter);
    }

    // Update guards: chapter numbers must stay unique within a novel, and changing to a duplicate number is rejected.
    @Test
    void updateChapterRejectsDuplicateChapterNumberWhenNumberChanges() {
        Novel novel = novel(10L);
        Chapter existingChapter = chapter(100L, novel, "Old Title", 1, "Old chapter content is long enough.");
        ChapterCreateDTO request = request("New Title", 2, "New chapter content is also long enough.");

        when(chapterRepository.findById(existingChapter.getId())).thenReturn(Optional.of(existingChapter));
        when(chapterRepository.existsByNovelIdAndChapterNumber(novel.getId(), request.getChapterNumber())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> chapterService.updateChapter(existingChapter.getId(), request));

        verify(chapterRepository, never()).save(any());
        verify(novelChapterInfoService, never()).refresh(any());
    }

    // Delete flow: removing a chapter also adjusts aggregate bookmark counts.
    @Test
    void deleteChapterRemovesChapterAndUpdatesNovelCounters() {
        Novel novel = novel(10L);
        Chapter chapter = chapter(100L, novel, "The First Door", 1, "This is a long enough chapter content.");

        when(chapterRepository.findById(chapter.getId())).thenReturn(Optional.of(chapter));
        when(bookmarkRepository.countByChapterId(chapter.getId())).thenReturn(3L);

        chapterService.deleteChapter(chapter.getId());

        verify(chapterRepository).delete(chapter);
        verify(chapterRepository).flush();
        verify(novelRepository).decrementBookmarksBy(novel.getId(), 3L);
        verify(novelChapterInfoService).prepareForChapterDeletion(novel.getId(), chapter.getId());
        verify(novelChapterInfoService, never()).refresh(novel.getId());
    }

    private static ChapterCreateDTO request(String title, Integer chapterNumber, String content) {
        return ChapterCreateDTO.builder()
            .title(title)
            .chapterNumber(chapterNumber)
            .content(content)
            .build();
    }

    private static Novel novel(Long id) {
        return Novel.builder()
            .id(id)
            .title("NetNovel Sample")
            .author("Author")
            .build();
    }

    private static Chapter chapter(Long id, Novel novel, String title, Integer chapterNumber, String content) {
        Chapter chapter = Chapter.builder()
            .id(id)
            .novel(novel)
            .title(title)
            .chapterNumber(chapterNumber)
            .updateAt(LocalDateTime.now())
            .build();
        chapter.setContent(content);
        return chapter;
    }
}
