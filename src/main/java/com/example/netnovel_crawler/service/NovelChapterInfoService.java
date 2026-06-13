package com.example.netnovel_crawler.service;

import com.example.netnovel_crawler.entity.Chapter;
import com.example.netnovel_crawler.entity.Novel;
import com.example.netnovel_crawler.entity.NovelChapterInfo;
import com.example.netnovel_crawler.repository.ChapterRepository;
import com.example.netnovel_crawler.repository.NovelChapterInfoRepository;
import com.example.netnovel_crawler.repository.NovelRepository;
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
            .orElseThrow(() -> new IllegalArgumentException("Novel not found: " + novelId));
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

        return novelChapterInfoRepository.save(info);
    }
}
