package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.ChapterContentDTO;
import com.example.netnovel_server.dto.ChapterCreateDTO;
import com.example.netnovel_server.dto.ChapterDTO;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.ChapterMapper;
import com.example.netnovel_server.repository.*;
import org.springframework.data.domain.Page;
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
        ensureNovelExists(novelId);
        return chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId, pageable).map(ChapterMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ChapterDTO> getAllChapters(Long novelId) {
        ensureNovelExists(novelId);
        return chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId).stream()
            .map(ChapterMapper::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public ChapterContentDTO getChapter(Long chapterId) {
        return ChapterMapper.toContentDTO(findChapter(chapterId));
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

    private void ensureNovelExists(Long novelId) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel not found");
        }
    }

}
