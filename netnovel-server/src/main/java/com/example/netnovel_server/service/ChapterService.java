package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.ChapterContentDTO;
import com.example.netnovel_server.dto.ChapterCreateDTO;
import com.example.netnovel_server.dto.ChapterDTO;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.NovelAccessStatus;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.ChapterMapper;
import com.example.netnovel_server.repository.*;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;
    private final BookmarkRepository bookmarkRepository;
    private final NovelChapterInfoService novelChapterInfoService;

    public ChapterService(
        ChapterRepository chapterRepository,
        NovelRepository novelRepository,
        BookmarkRepository bookmarkRepository,
        NovelChapterInfoService novelChapterInfoService
    ) {
        this.chapterRepository = chapterRepository;
        this.novelRepository = novelRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.novelChapterInfoService = novelChapterInfoService;
    }

    @Transactional(readOnly = true)
    public Page<ChapterDTO> getChapters(Long novelId, Pageable pageable) {
        Novel novel = findNovel(novelId);
        if (isPreviewLimitedForCurrentUser(novel)) {
            List<ChapterDTO> previewChapters = chapterRepository.findTop3ByNovelIdOrderByChapterNumberAsc(novelId)
                .stream()
                .map(ChapterMapper::toDTO)
                .toList();
            return toPage(previewChapters, pageable);
        }

        return chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId, pageable).map(ChapterMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ChapterDTO> getAllChapters(Long novelId) {
        Novel novel = findNovel(novelId);
        if (isPreviewLimitedForCurrentUser(novel)) {
            return chapterRepository.findTop3ByNovelIdOrderByChapterNumberAsc(novelId).stream()
                .map(ChapterMapper::toDTO)
                .toList();
        }

        return chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId).stream()
            .map(ChapterMapper::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public ChapterContentDTO getChapter(Long chapterId) {
        Chapter chapter = findChapter(chapterId);
        if (isPreviewLimitedForCurrentUser(chapter.getNovel()) && !isPreviewChapter(chapter)) {
            throw new ResourceNotFoundException("Chapter not found");
        }

        return ChapterMapper.toContentDTO(chapter);
    }

    @Transactional
    public ChapterContentDTO createChapter(Long novelId, ChapterCreateDTO request) {
        Novel novel = novelRepository.findById(novelId)
            .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));

        if (chapterRepository.existsByNovelIdAndChapterNumber(novelId, request.getChapterNumber())) {
            throw new DuplicateResourceException("Chapter number already exists in this novel");
        }

        Chapter chapter = chapterRepository.save(ChapterMapper.toEntity(request, novel));
        novelRepository.advanceUpdateAt(novelId, chapter.getUpdateAt());
        novelChapterInfoService.refresh(novelId);
        return ChapterMapper.toContentDTO(chapter);
    }

    @Transactional
    public ChapterContentDTO updateChapter(Long chapterId, ChapterCreateDTO request) {
        Chapter chapter = findChapter(chapterId);
        Long novelId = chapter.getNovel().getId();

        if (!chapter.getChapterNumber().equals(request.getChapterNumber())
            && chapterRepository.existsByNovelIdAndChapterNumber(novelId, request.getChapterNumber())) {
            throw new DuplicateResourceException("Chapter number already exists in this novel");
        }

        chapter.setTitle(request.getTitle());
        chapter.setChapterNumber(request.getChapterNumber());
        chapter.setContent(request.getContent());

        Chapter savedChapter = chapterRepository.save(chapter);
        novelRepository.advanceUpdateAt(novelId, savedChapter.getUpdateAt());
        novelChapterInfoService.refresh(novelId);
        return ChapterMapper.toContentDTO(savedChapter);
    }

    @Transactional
    public void deleteChapter(Long chapterId) {
        Chapter chapter = findChapter(chapterId);
        Novel novel = chapter.getNovel();
        Long novelId = novel.getId();
        long bookmarkCount = bookmarkRepository.countByChapterId(chapterId);
        chapterRepository.delete(chapter);
        chapterRepository.flush();
        novelRepository.decrementBookmarksBy(novelId, bookmarkCount);
        novelChapterInfoService.refresh(novelId);
    }

    private Chapter findChapter(Long chapterId) {
        return chapterRepository.findById(chapterId)
            .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
    }

    private Novel findNovel(Long novelId) {
        return novelRepository.findById(novelId)
            .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));
    }

    private boolean isPreviewLimitedForCurrentUser(Novel novel) {
        NovelAccessStatus accessStatus = novel.getAccessStatus() != null
            ? novel.getAccessStatus()
            : NovelAccessStatus.NORMAL;

        return accessStatus == NovelAccessStatus.PREVIEW_ONLY
            && !SecurityUtils.hasAnyRole("MANAGER", "ADMIN");
    }

    private boolean isPreviewChapter(Chapter chapter) {
        return chapterRepository.countByNovelIdAndChapterNumberLessThan(
            chapter.getNovel().getId(),
            chapter.getChapterNumber()
        ) < 3;
    }

    private Page<ChapterDTO> toPage(List<ChapterDTO> chapters, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(chapters);
        }

        int start = Math.toIntExact(Math.min(pageable.getOffset(), chapters.size()));
        int end = Math.min(start + pageable.getPageSize(), chapters.size());
        return new PageImpl<>(chapters.subList(start, end), pageable, chapters.size());
    }

}
