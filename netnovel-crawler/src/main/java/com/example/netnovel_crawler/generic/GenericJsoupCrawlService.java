package com.example.netnovel_crawler.generic;

import com.example.netnovel_crawler.dto.CrawlNovelRequestMessage;
import com.example.netnovel_crawler.entity.Chapter;
import com.example.netnovel_crawler.entity.Novel;
import com.example.netnovel_crawler.persistence.CrawlPersistenceService;
import com.example.netnovel_crawler.persistence.CrawledNovelData;
import com.example.netnovel_crawler.profile.CrawlerSourceProfile;
import com.example.netnovel_crawler.repository.CrawlerSourceProfileRepository;
import com.example.netnovel_crawler.source.CrawlerSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
public class GenericJsoupCrawlService {

    private static final Logger log = LoggerFactory.getLogger(GenericJsoupCrawlService.class);
    private static final long CHAPTER_FETCH_DELAY_MILLIS = 200L;

    private final CrawlerSourceProfileRepository crawlerSourceProfileRepository;
    private final GenericJsoupCrawler genericJsoupCrawler;
    private final CrawlPersistenceService crawlPersistenceService;

    public GenericJsoupCrawlService(
        CrawlerSourceProfileRepository crawlerSourceProfileRepository,
        GenericJsoupCrawler genericJsoupCrawler,
        CrawlPersistenceService crawlPersistenceService
    ) {
        this.crawlerSourceProfileRepository = crawlerSourceProfileRepository;
        this.genericJsoupCrawler = genericJsoupCrawler;
        this.crawlPersistenceService = crawlPersistenceService;
    }

    @Transactional
    public void crawlNovel(CrawlerSource source, CrawlNovelRequestMessage message) {
        if (source.profileId() == null) {
            throw new IllegalArgumentException("Generic Jsoup crawl requires a source profile id.");
        }

        CrawlerSourceProfile profile = crawlerSourceProfileRepository.findById(source.profileId())
            .orElseThrow(() -> new IllegalArgumentException("Crawler source profile not found: " + source.profileId()));

        log.info(
            "Starting dynamic generic Jsoup crawl. taskId={}, profileId={}, source={}, url={}",
            message.getTaskId(),
            profile.getId(),
            source.name(),
            message.getUrl()
        );

        GenericNovelExtractionResult novelData = genericJsoupCrawler.crawlNovel(profile, message.getUrl());
        Novel novel = crawlPersistenceService.upsertNovel(new CrawledNovelData(
            novelData.sourceName(),
            profile.getDomain(),
            novelData.sourceNovelUrl(),
            extractExternalId(novelData.sourceNovelUrl()),
            novelData.title(),
            novelData.author(),
            novelData.description(),
            novelData.coverImageUrl(),
            novelData.genres(),
            novelData.tags()
        ));

        int fallbackChapterNumber = 1;
        for (GenericChapterLink chapterLink : novelData.chapters()) {
            int chapterNumber = chapterLink.chapterNumber() != null ? chapterLink.chapterNumber() : fallbackChapterNumber;
            fallbackChapterNumber = Math.max(fallbackChapterNumber + 1, chapterNumber + 1);

            if (crawlPersistenceService.hasSuccessfulChapter(source.name(), chapterLink.url())) {
                log.info(
                    "Skipping already successful dynamic chapter. taskId={}, novelId={}, chapterNumber={}, url={}",
                    message.getTaskId(),
                    novel.getId(),
                    chapterNumber,
                    chapterLink.url()
                );
                continue;
            }

            crawlChapter(profile, source, novel, chapterLink, chapterNumber, message.getTaskId());
        }

        crawlPersistenceService.refreshChapterInfo(novel.getId());
        log.info("Finished dynamic generic Jsoup crawl. taskId={}, profileId={}, novelId={}",
            message.getTaskId(),
            profile.getId(),
            novel.getId()
        );
    }

    private void crawlChapter(
        CrawlerSourceProfile profile,
        CrawlerSource source,
        Novel novel,
        GenericChapterLink chapterLink,
        int chapterNumber,
        Long taskId
    ) {
        try {
            delayBeforeChapterFetch(chapterLink.url());
            GenericChapterExtractionResult chapterData = genericJsoupCrawler.crawlChapter(
                profile,
                new GenericChapterLink(chapterNumber, chapterLink.title(), chapterLink.url())
            );
            Chapter savedChapter = crawlPersistenceService.saveSuccessfulChapter(
                novel,
                chapterNumber,
                chapterData.title(),
                chapterData.content(),
                source.name(),
                chapterLink.url()
            );
            log.info(
                "Dynamic chapter crawl success. taskId={}, novelId={}, chapterId={}, chapterNumber={}, title=\"{}\", contentLength={}",
                taskId,
                novel.getId(),
                savedChapter.getId(),
                chapterNumber,
                chapterData.title(),
                chapterData.content().length()
            );
        } catch (Exception exception) {
            crawlPersistenceService.saveFailedChapter(novel, source.name(), chapterLink.url(), exception.getMessage());
            log.warn(
                "Dynamic chapter crawl failed. taskId={}, novelId={}, chapterNumber={}, url={}, error={}",
                taskId,
                novel.getId(),
                chapterNumber,
                chapterLink.url(),
                exception.getMessage()
            );
        }
    }

    private void delayBeforeChapterFetch(String chapterUrl) {
        try {
            Thread.sleep(CHAPTER_FETCH_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted before fetching chapter URL: " + chapterUrl, exception);
        }
    }

    private String extractExternalId(String sourceNovelUrl) {
        try {
            String path = URI.create(sourceNovelUrl).getPath();
            if (path == null || path.isBlank()) {
                return sourceNovelUrl;
            }
            String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
            int slashIndex = trimmed.lastIndexOf('/');
            return slashIndex >= 0 ? trimmed.substring(slashIndex + 1) : trimmed;
        } catch (IllegalArgumentException exception) {
            return sourceNovelUrl;
        }
    }
}
