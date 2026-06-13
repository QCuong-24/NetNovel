package com.example.netnovel_crawler.wuxiaworld;

import com.example.netnovel_crawler.dto.CrawlNovelRequestMessage;
import com.example.netnovel_crawler.entity.*;
import com.example.netnovel_crawler.repository.*;
import com.example.netnovel_crawler.service.NovelChapterInfoService;
import com.example.netnovel_crawler.source.CrawlerSource;
import com.example.netnovel_crawler.utility.TextCleaner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WuxiaworldJsoupCrawler {

    private static final Logger log = LoggerFactory.getLogger(WuxiaworldJsoupCrawler.class);

    private static final String CRAWLED_TAG = "Crawled";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");
    private static final String SOURCE_MARKER_TEMPLATE = "[Crawled Source: %s]";
    private static final long CHAPTER_FETCH_DELAY_MILLIS = 200L;

    private final WuxiaworldProperties properties;
    private final NovelRepository novelRepository;
    private final NovelSourceRepository novelSourceRepository;
    private final ChapterRepository chapterRepository;
    private final TagRepository tagRepository;
    private final CrawlChapterRecordRepository crawlChapterRecordRepository;
    private final NovelChapterInfoService novelChapterInfoService;

    public WuxiaworldJsoupCrawler(
        WuxiaworldProperties properties,
        NovelRepository novelRepository,
        NovelSourceRepository novelSourceRepository,
        ChapterRepository chapterRepository,
        TagRepository tagRepository,
        CrawlChapterRecordRepository crawlChapterRecordRepository,
        NovelChapterInfoService novelChapterInfoService
    ) {
        this.properties = properties;
        this.novelRepository = novelRepository;
        this.novelSourceRepository = novelSourceRepository;
        this.chapterRepository = chapterRepository;
        this.tagRepository = tagRepository;
        this.crawlChapterRecordRepository = crawlChapterRecordRepository;
        this.novelChapterInfoService = novelChapterInfoService;
    }

    @Transactional
    public void crawlNovel(CrawlerSource source, CrawlNovelRequestMessage message) {
        log.info("Starting Wuxiaworld novel crawl. taskId={}, url={}", message.getTaskId(), message.getUrl());
        Document novelDocument = fetch(message.getUrl());
        String title = requiredText(novelDocument, properties.titleSelector(), "novel title");
        String author = requiredText(novelDocument, properties.authorSelector(), "novel author");
        String description = appendSourceMarker(
            requiredText(novelDocument, properties.descriptionSelector(), "novel description"),
            message.getUrl()
        );
        int totalChapters = parseTotalChapters(requiredText(
            novelDocument,
            properties.totalChaptersSelector(),
            "total chapters"
        ));
        log.info(
            "Parsed Wuxiaworld novel detail. taskId={}, title=\"{}\", author=\"{}\", totalChapters={}",
            message.getTaskId(),
            title,
            author,
            totalChapters
        );

        Novel novel = upsertNovel(source, message.getUrl(), title, author, description);
        log.info("Novel upserted. taskId={}, novelId={}, title=\"{}\"", message.getTaskId(), novel.getId(), novel.getTitle());
        String slug = extractSlug(message.getUrl());
        for (int chapterNumber = 1; chapterNumber <= totalChapters; chapterNumber++) {
            String chapterUrl = buildChapterUrl(slug, chapterNumber);
            if (crawlChapterRecordRepository.existsBySourceNameAndSourceChapterUrlAndStatus(
                source.name(),
                chapterUrl,
                CrawlChapterStatus.SUCCESS
            )) {
                log.info(
                    "Skipping already successful chapter. taskId={}, novelId={}, chapterNumber={}, url={}",
                    message.getTaskId(),
                    novel.getId(),
                    chapterNumber,
                    chapterUrl
                );
                continue;
            }
            crawlChapter(source, novel, chapterNumber, chapterUrl, message.getTaskId());
        }
        novelChapterInfoService.refresh(novel.getId());
        log.info("Finished Wuxiaworld novel crawl loop. taskId={}, novelId={}", message.getTaskId(), novel.getId());
    }

    private void crawlChapter(CrawlerSource source, Novel novel, int chapterNumber, String chapterUrl, Long taskId) {
        try {
            log.info(
                "Crawling chapter. taskId={}, novelId={}, chapterNumber={}, url={}",
                taskId,
                novel.getId(),
                chapterNumber,
                chapterUrl
            );
            Document chapterDocument = fetchChapter(chapterUrl);
            ChapterText chapterText = extractChapterText(chapterDocument, chapterNumber);
            String title = chapterText.title();
            String content = chapterText.content();
            if (content.isBlank()) {
                throw new IllegalStateException("Chapter content is empty");
            }

            Chapter chapter = chapterRepository.findByNovelIdAndChapterNumber(novel.getId(), chapterNumber)
                .orElseGet(() -> Chapter.builder()
                    .novel(novel)
                    .chapterNumber(chapterNumber)
                    .build());
            chapter.setTitle(title);
            chapter.setContent(content);
            Chapter savedChapter = chapterRepository.save(chapter);
            saveChapterRecord(source.name(), chapterUrl, novel, savedChapter, CrawlChapterStatus.SUCCESS, null);
            log.info(
                "Chapter crawl success. taskId={}, novelId={}, chapterId={}, chapterNumber={}, title=\"{}\", contentLength={}",
                taskId,
                novel.getId(),
                savedChapter.getId(),
                chapterNumber,
                title,
                content.length()
            );
        } catch (Exception exception) {
            saveChapterRecord(source.name(), chapterUrl, novel, null, CrawlChapterStatus.FAILED, exception.getMessage());
            log.warn(
                "Chapter crawl failed. taskId={}, novelId={}, chapterNumber={}, url={}, error={}",
                taskId,
                novel.getId(),
                chapterNumber,
                chapterUrl,
                exception.getMessage()
            );
        }
    }

    private Novel upsertNovel(CrawlerSource source, String sourceNovelUrl, String title, String author, String description) {
        Optional<NovelSource> existingSource = novelSourceRepository.findBySourceNameAndSourceNovelUrl(
            source.name(),
            sourceNovelUrl
        );
        if (existingSource.isPresent()) {
            Novel novel = existingSource.get().getNovel();
            novel.setTitle(title);
            novel.setAuthor(author);
            novel.setDescription(appendSourceMarker(novel.getDescription(), sourceNovelUrl));
            novel.getTags().add(resolveCrawledTag());
            Novel savedNovel = novelRepository.save(novel);
            existingSource.get().setLastCrawledAt(LocalDateTime.now());
            novelSourceRepository.save(existingSource.get());
            log.info("Updated existing crawled novel source. source={}, sourceUrl={}, novelId={}", source.name(), sourceNovelUrl, savedNovel.getId());
            return savedNovel;
        }

        Novel novel = Novel.builder()
            .title(title)
            .author(author)
            .description(description)
            .status(Status.ONGOING)
            .build();
        novel.getTags().add(resolveCrawledTag());
        Novel savedNovel = novelRepository.save(novel);
        novelSourceRepository.save(NovelSource.builder()
            .novel(savedNovel)
            .sourceName(source.name())
            .sourceNovelUrl(sourceNovelUrl)
            .externalId(extractSlug(sourceNovelUrl))
            .lastCrawledAt(LocalDateTime.now())
            .build());
        log.info("Created new crawled novel source. source={}, sourceUrl={}, novelId={}", source.name(), sourceNovelUrl, savedNovel.getId());
        return savedNovel;
    }

    private Tag resolveCrawledTag() {
        return tagRepository.findByName(CRAWLED_TAG)
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

    private Document fetch(String url) {
        try {
            log.debug("Fetching URL: {}", url);
            return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(30_000)
                .get();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to fetch URL: " + url, exception);
        }
    }

    private Document fetchChapter(String url) {
        try {
            Thread.sleep(CHAPTER_FETCH_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted before fetching chapter URL: " + url, exception);
        }

        return fetch(url);
    }

    private String requiredText(Document document, String selector, String label) {
        return optionalText(document, selector)
            .filter(value -> !value.isBlank())
            .orElseThrow(() -> new IllegalStateException("Missing " + label + " with selector: " + selector));
    }

    private Optional<String> optionalText(Document document, String selector) {
        Element element = document.selectFirst(selector);
        if (element == null) {
            return Optional.empty();
        }
        return Optional.of(TextCleaner.cleanInline(element.text()));
    }

    private ChapterText extractChapterText(Document document, int chapterNumber) {
        Elements contentElements = document.select(properties.chapterContentSelector());
        if (contentElements.isEmpty()) {
            return new ChapterText("Chapter " + chapterNumber, "");
        }

        StringBuilder contentBuilder = new StringBuilder();
        for (Element contentElement : contentElements) {
            String paragraph = TextCleaner.cleanContent(contentElement.wholeText());
            if (paragraph.isBlank()) {
                continue;
            }
            if (!contentBuilder.isEmpty()) {
                contentBuilder.append("\n\n");
            }
            contentBuilder.append(paragraph);
        }

        return new ChapterText("Chapter " + chapterNumber, TextCleaner.cleanContent(contentBuilder.toString()));
    }

    private int parseTotalChapters(String value) {
        Matcher matcher = NUMBER_PATTERN.matcher(value.replace(",", ""));
        if (!matcher.find()) {
            throw new IllegalStateException("Could not parse total chapters from: " + value);
        }
        return Integer.parseInt(matcher.group(1));
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

    private String extractSlug(String novelUrl) {
        String path = URI.create(novelUrl).getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    private String buildChapterUrl(String slug, int chapterNumber) {
        return "https://wuxiaworld.eu/chapter/" + slug + "-" + chapterNumber;
    }

    private record ChapterText(String title, String content) {
    }
}
