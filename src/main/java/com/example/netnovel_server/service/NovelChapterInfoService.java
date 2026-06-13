package com.example.netnovel_server.service;

import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.NovelChapterInfo;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.repository.ChapterRepository;
import com.example.netnovel_server.repository.NovelChapterInfoRepository;
import com.example.netnovel_server.repository.NovelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NovelChapterInfoService {

    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final NovelChapterInfoRepository novelChapterInfoRepository;

    public NovelChapterInfoService(
        NovelRepository novelRepository,
        ChapterRepository chapterRepository,
        NovelChapterInfoRepository novelChapterInfoRepository
    ) {
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.novelChapterInfoRepository = novelChapterInfoRepository;
    }

    @Transactional
    public NovelChapterInfo refresh(Long novelId) {
        Novel novel = novelRepository.findById(novelId)
            .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));

        long chapterCount = chapterRepository.countByNovelId(novelId);
        Chapter latestChapter = chapterRepository.findTopByNovelIdOrderByChapterNumberDesc(novelId).orElse(null);

        NovelChapterInfo info = novelChapterInfoRepository.findById(novelId)
            .orElseGet(() -> NovelChapterInfo.builder()
                .novel(novel)
                .build());

        info.setNovel(novel);
        info.setChapterCount(Math.toIntExact(chapterCount));
        info.setLatestChapter(latestChapter);
        info.setLatestChapterNumber(latestChapter == null ? null : latestChapter.getChapterNumber());
        info.setLatestChapterTitle(latestChapter == null ? null : latestChapter.getTitle());
        info.setLatestChapterUpdatedAt(latestChapter == null ? null : latestChapter.getUpdateAt());

        NovelChapterInfo savedInfo = novelChapterInfoRepository.save(info);
        novel.setChapterInfo(savedInfo);
        return savedInfo;
    }

    @Transactional
    public void refreshAll() {
        novelRepository.findAll().forEach(novel -> refresh(novel.getId()));
    }
}
