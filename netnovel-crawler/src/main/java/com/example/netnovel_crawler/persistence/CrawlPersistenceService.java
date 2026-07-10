package com.example.netnovel_crawler.persistence;

import com.example.netnovel_crawler.entity.*;
import com.example.netnovel_crawler.repository.*;
import com.example.netnovel_crawler.service.NovelChapterInfoService;
import com.example.netnovel_crawler.utility.TextCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class CrawlPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(CrawlPersistenceService.class);

    private static final String CRAWLED_TAG = "Crawled";
    private static final String SOURCE_MARKER_TEMPLATE = "[Crawled Source: %s]";

    private final NovelRepository novelRepository;
    private final NovelSourceRepository novelSourceRepository;
    private final ChapterRepository chapterRepository;
    private final GenreRepository genreRepository;
    private final TagRepository tagRepository;
    private final CrawlChapterRecordRepository crawlChapterRecordRepository;
    private final NovelChapterInfoService novelChapterInfoService;

    public CrawlPersistenceService(
        NovelRepository novelRepository,
        NovelSourceRepository novelSourceRepository,
        ChapterRepository chapterRepository,
        GenreRepository genreRepository,
        TagRepository tagRepository,
        CrawlChapterRecordRepository crawlChapterRecordRepository,
        NovelChapterInfoService novelChapterInfoService
    ) {
        this.novelRepository = novelRepository;
        this.novelSourceRepository = novelSourceRepository;
        this.chapterRepository = chapterRepository;
        this.genreRepository = genreRepository;
        this.tagRepository = tagRepository;
        this.crawlChapterRecordRepository = crawlChapterRecordRepository;
        this.novelChapterInfoService = novelChapterInfoService;
    }

    @Transactional
    public Novel upsertNovel(CrawledNovelData data) {
        return novelSourceRepository.findBySourceNameAndSourceNovelUrl(data.sourceName(), data.sourceNovelUrl())
            .map(existingSource -> updateExistingNovel(existingSource, data))
            .orElseGet(() -> createNovel(data));
    }

    @Transactional(readOnly = true)
    public boolean hasSuccessfulChapter(String sourceName, String chapterUrl) {
        return crawlChapterRecordRepository.existsBySourceNameAndSourceChapterUrlAndStatus(
            sourceName,
            chapterUrl,
            CrawlChapterStatus.SUCCESS
        );
    }

    @Transactional
    public Chapter saveSuccessfulChapter(
        Novel novel,
        Integer chapterNumber,
        String chapterTitle,
        String chapterContent,
        String sourceName,
        String sourceChapterUrl
    ) {
        Chapter chapter = chapterRepository.findByNovelIdAndChapterNumber(novel.getId(), chapterNumber)
            .orElseGet(() -> Chapter.builder()
                .novel(novel)
                .chapterNumber(chapterNumber)
                .build());
        chapter.setTitle(chapterTitle);
        chapter.setContent(chapterContent);
        Chapter savedChapter = chapterRepository.save(chapter);
        novelRepository.advanceUpdateAt(novel.getId(), savedChapter.getUpdateAt());
        saveChapterRecord(sourceName, sourceChapterUrl, novel, savedChapter, CrawlChapterStatus.SUCCESS, null);
        return savedChapter;
    }

    @Transactional
    public void saveFailedChapter(
        Novel novel,
        String sourceName,
        String sourceChapterUrl,
        String errorMessage
    ) {
        saveChapterRecord(sourceName, sourceChapterUrl, novel, null, CrawlChapterStatus.FAILED, errorMessage);
    }

    @Transactional
    public void refreshChapterInfo(Long novelId) {
        novelChapterInfoService.refresh(novelId);
    }

    private Novel updateExistingNovel(NovelSource existingSource, CrawledNovelData data) {
        Novel novel = existingSource.getNovel();
        novel.setTitle(data.title());
        novel.setAuthor(data.author());
        novel.setDescription(appendSourceMarker(novel.getDescription(), data.sourceNovelUrl()));
        addGenres(novel, data.genres());
        addTags(novel, data.tags());
        novel.getTags().add(resolveCrawledTag());
        updateCoverFromCrawl(novel, data.coverImageUrl(), data.sourceDomain());
        Novel savedNovel = novelRepository.save(novel);
        existingSource.setLastCrawledAt(LocalDateTime.now());
        novelSourceRepository.save(existingSource);
        log.info("Updated existing crawled novel source. source={}, sourceUrl={}, novelId={}",
            data.sourceName(),
            data.sourceNovelUrl(),
            savedNovel.getId()
        );
        return savedNovel;
    }

    private Novel createNovel(CrawledNovelData data) {
        Novel novel = Novel.builder()
            .title(data.title())
            .author(data.author())
            .description(appendSourceMarker(data.description(), data.sourceNovelUrl()))
            .coverImageUrl(blankToNull(data.coverImageUrl()))
            .status(Status.ONGOING)
            .build();
        addGenres(novel, data.genres());
        addTags(novel, data.tags());
        novel.getTags().add(resolveCrawledTag());
        Novel savedNovel = novelRepository.save(novel);
        novelSourceRepository.save(NovelSource.builder()
            .novel(savedNovel)
            .sourceName(data.sourceName())
            .sourceNovelUrl(data.sourceNovelUrl())
            .externalId(data.externalId())
            .lastCrawledAt(LocalDateTime.now())
            .build());
        log.info("Created new crawled novel source. source={}, sourceUrl={}, novelId={}",
            data.sourceName(),
            data.sourceNovelUrl(),
            savedNovel.getId()
        );
        return savedNovel;
    }

    private void addGenres(Novel novel, Set<String> genreNames) {
        if (genreNames == null) {
            return;
        }
        for (String genreName : genreNames) {
            String normalized = normalizeName(genreName);
            if (!normalized.isBlank()) {
                novel.getGenres().add(resolveGenre(normalized));
            }
        }
    }

    private void addTags(Novel novel, Set<String> tagNames) {
        if (tagNames == null) {
            return;
        }
        for (String tagName : tagNames) {
            String normalized = normalizeName(tagName);
            if (!normalized.isBlank()) {
                novel.getTags().add(resolveTag(normalized));
            }
        }
    }

    private Genre resolveGenre(String genreName) {
        return genreRepository.findByNameIgnoreCase(genreName)
            .orElseGet(() -> genreRepository.save(Genre.builder().name(genreName).build()));
    }

    private Tag resolveTag(String tagName) {
        return tagRepository.findByNameIgnoreCase(tagName)
            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
    }

    private Tag resolveCrawledTag() {
        return tagRepository.findByNameIgnoreCase(CRAWLED_TAG)
            .orElseGet(() -> tagRepository.save(Tag.builder().name(CRAWLED_TAG).build()));
    }

    private void saveChapterRecord(
        String sourceName,
        String chapterUrl,
        Novel novel,
        Chapter chapter,
        CrawlChapterStatus status,
        String errorMessage
    ) {
        CrawlChapterRecord record = crawlChapterRecordRepository
            .findBySourceNameAndSourceChapterUrl(sourceName, chapterUrl)
            .orElseGet(() -> CrawlChapterRecord.builder()
                .sourceName(sourceName)
                .sourceChapterUrl(chapterUrl)
                .novel(novel)
                .build());
        record.setNovel(novel);
        record.setChapter(chapter);
        record.setStatus(status);
        record.setErrorMessage(errorMessage);
        record.setCrawledAt(LocalDateTime.now());
        crawlChapterRecordRepository.save(record);
    }

    private void updateCoverFromCrawl(Novel novel, String crawledCoverImageUrl, String sourceDomain) {
        if (crawledCoverImageUrl == null || crawledCoverImageUrl.isBlank()) {
            return;
        }
        if (novel.getCoverImagePublicId() != null && !novel.getCoverImagePublicId().isBlank()) {
            log.info("Keeping managed cover image. novelId={}", novel.getId());
            return;
        }

        String existingCoverImageUrl = novel.getCoverImageUrl();
        if (existingCoverImageUrl == null || existingCoverImageUrl.isBlank() || isFromSourceDomain(existingCoverImageUrl, sourceDomain)) {
            novel.setCoverImageUrl(crawledCoverImageUrl);
            log.info("Updated crawled cover image. novelId={}", novel.getId());
        } else {
            log.info("Keeping non-source cover image. novelId={}", novel.getId());
        }
    }

    private boolean isFromSourceDomain(String url, String sourceDomain) {
        try {
            if (sourceDomain == null || sourceDomain.isBlank()) {
                return false;
            }
            String host = URI.create(url).getHost();
            if (host == null) {
                return false;
            }
            String normalizedHost = host.toLowerCase();
            String normalizedSourceDomain = sourceDomain.toLowerCase();
            return normalizedHost.equals(normalizedSourceDomain) || normalizedHost.endsWith("." + normalizedSourceDomain);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String appendSourceMarker(String description, String sourceUrl) {
        String marker = SOURCE_MARKER_TEMPLATE.formatted(sourceUrl);
        String cleanedDescription = TextCleaner.cleanContent(description);
        if (cleanedDescription.contains(marker)) {
            return cleanedDescription;
        }
        if (cleanedDescription.isBlank()) {
            return marker;
        }
        return cleanedDescription + "\n\n" + marker;
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }

        String normalized = TextCleaner.cleanInline(value).trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            return "";
        }
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
