package com.example.netnovel_crawler.generic;

import com.example.netnovel_crawler.profile.ChapterDiscoveryType;
import com.example.netnovel_crawler.profile.CrawlerSourceProfile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GenericJsoupCrawler {

    private static final Logger log = LoggerFactory.getLogger(GenericJsoupCrawler.class);
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; NetNovelCrawler/1.0)";
    private static final int TIMEOUT_MILLIS = 20_000;
    private static final Pattern TRAILING_NUMBER_PATTERN = Pattern.compile("(\\d+)(?:/)?$");

    public GenericNovelExtractionResult crawlNovel(CrawlerSourceProfile profile, String novelUrl) {
        log.info("Starting generic Jsoup novel extraction. profileId={}, domain={}, url={}",
            profile.getId(),
            profile.getDomain(),
            novelUrl
        );

        Document document = fetch(novelUrl);
        String title = requiredText(document, profile.getTitleSelector(), "novel title");
        String author = textOrDefault(document, profile.getAuthorSelector(), "Unknown");
        String description = textOrDefault(document, profile.getDescriptionSelector(), "");
        String coverImageUrl = imageUrl(document, profile.getCoverImageSelector());
        Set<String> genres = names(document, profile.getGenreSelector());
        Set<String> tags = names(document, profile.getTagSelector());
        List<GenericChapterLink> chapters = discoverChapters(profile, document, novelUrl);

        log.info(
            "Finished generic Jsoup novel extraction. profileId={}, title=\"{}\", author=\"{}\", chapters={}, genres={}, tags={}, hasCoverImage={}",
            profile.getId(),
            title,
            author,
            chapters.size(),
            genres,
            tags,
            !coverImageUrl.isBlank()
        );

        return new GenericNovelExtractionResult(
            profile.getSourceName(),
            novelUrl,
            title,
            author,
            description,
            coverImageUrl,
            genres,
            tags,
            chapters
        );
    }

    public GenericChapterExtractionResult crawlChapter(
        CrawlerSourceProfile profile,
        GenericChapterLink chapterLink
    ) {
        log.info("Starting generic Jsoup chapter extraction. profileId={}, chapterNumber={}, url={}",
            profile.getId(),
            chapterLink.chapterNumber(),
            chapterLink.url()
        );

        Document document = fetch(chapterLink.url());
        String title = textOrDefault(document, profile.getChapterTitleSelector(), chapterLink.title());
        if (title.isBlank() && chapterLink.chapterNumber() != null) {
            title = "Chapter " + chapterLink.chapterNumber();
        }

        String content = requiredJoinedText(document, profile.getChapterContentSelector(), "chapter content");

        log.info(
            "Finished generic Jsoup chapter extraction. profileId={}, chapterNumber={}, title=\"{}\", contentLength={}",
            profile.getId(),
            chapterLink.chapterNumber(),
            title,
            content.length()
        );

        return new GenericChapterExtractionResult(
            chapterLink.chapterNumber(),
            chapterLink.url(),
            title,
            content
        );
    }

    private List<GenericChapterLink> discoverChapters(
        CrawlerSourceProfile profile,
        Document document,
        String novelUrl
    ) {
        ChapterDiscoveryType discoveryType = profile.getChapterDiscoveryType();
        if (discoveryType == ChapterDiscoveryType.NUMBER_RANGE) {
            return discoverNumberRangeChapters(profile, document, novelUrl);
        }
        if (discoveryType == ChapterDiscoveryType.PAGINATED_LIST) {
            throw new UnsupportedOperationException("Generic Jsoup crawler does not support paginated chapter lists yet.");
        }
        return discoverListedChapters(profile, document);
    }

    private List<GenericChapterLink> discoverListedChapters(CrawlerSourceProfile profile, Document document) {
        String selector = firstPresent(profile.getChapterUrlSelector(), profile.getChapterListSelector());
        if (selector.isBlank()) {
            throw new IllegalStateException("Chapter URL selector is required for LIST chapter discovery.");
        }

        Elements elements = document.select(selector);
        List<GenericChapterLink> chapters = new ArrayList<>();
        Set<String> seenUrls = new LinkedHashSet<>();
        for (Element element : elements) {
            Element link = "a".equalsIgnoreCase(element.tagName()) ? element : element.selectFirst("a[href]");
            if (link == null) {
                continue;
            }

            String chapterUrl = link.absUrl("href").trim();
            if (chapterUrl.isBlank() || !seenUrls.add(chapterUrl)) {
                continue;
            }

            chapters.add(new GenericChapterLink(
                extractTrailingNumber(chapterUrl),
                normalizeWhitespace(link.text()),
                chapterUrl
            ));
        }

        if (chapters.isEmpty()) {
            throw new IllegalStateException("No chapter links found with selector: " + selector);
        }
        return List.copyOf(chapters);
    }

    private List<GenericChapterLink> discoverNumberRangeChapters(
        CrawlerSourceProfile profile,
        Document document,
        String novelUrl
    ) {
        String pattern = firstPresent(profile.getChapterUrlPattern(), "");
        if (pattern.isBlank()) {
            throw new IllegalStateException("Chapter URL pattern is required for NUMBER_RANGE chapter discovery.");
        }

        int totalChapters = parseFirstInteger(requiredText(document, profile.getTotalChapterSelector(), "total chapters"));
        String slug = extractLastPathSegment(novelUrl);
        List<GenericChapterLink> chapters = new ArrayList<>();
        for (int chapterNumber = 1; chapterNumber <= totalChapters; chapterNumber++) {
            String chapterUrl = buildChapterUrl(pattern, slug, chapterNumber);
            chapters.add(new GenericChapterLink(
                chapterNumber,
                "Chapter " + chapterNumber,
                chapterUrl
            ));
        }
        return List.copyOf(chapters);
    }

    private Document fetch(String url) {
        try {
            return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MILLIS)
                .get();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not fetch URL: " + url, exception);
        }
    }

    private String requiredText(Document document, String selector, String label) {
        String value = textOrDefault(document, selector, "");
        if (value.isBlank()) {
            throw new IllegalStateException("Could not extract " + label + " with selector: " + selector);
        }
        return value;
    }

    private String requiredJoinedText(Document document, String selector, String label) {
        String value = joinedText(document, selector);
        if (value.isBlank()) {
            throw new IllegalStateException("Could not extract " + label + " with selector: " + selector);
        }
        return value;
    }

    private String textOrDefault(Document document, String selector, String defaultValue) {
        if (selector == null || selector.isBlank()) {
            return defaultValue;
        }

        Element element = document.selectFirst(selector);
        if (element == null) {
            return defaultValue;
        }
        return normalizeWhitespace(elementValue(element));
    }

    private String joinedText(Document document, String selector) {
        if (selector == null || selector.isBlank()) {
            return "";
        }

        Elements elements = document.select(selector);
        if (elements.isEmpty()) {
            return "";
        }

        List<String> chunks = elements.stream()
            .map(Element::text)
            .map(this::normalizeWhitespace)
            .filter(text -> !text.isBlank())
            .toList();
        return String.join("\n\n", chunks);
    }

    private String imageUrl(Document document, String selector) {
        if (selector == null || selector.isBlank()) {
            return "";
        }

        Element element = document.selectFirst(selector);
        if (element == null) {
            return "";
        }

        String src = firstPresent(
            element.absUrl("src"),
            element.absUrl("data-src"),
            element.absUrl("data-lazy-src"),
            element.absUrl("content"),
            element.attr("content")
        );
        return src.startsWith("http://") || src.startsWith("https://") ? src : "";
    }

    private Set<String> names(Document document, String selector) {
        if (selector == null || selector.isBlank()) {
            return Set.of();
        }

        Set<String> names = new LinkedHashSet<>();
        for (Element element : document.select(selector)) {
            String name = normalizeWhitespace(elementValue(element));
            if (!name.isBlank()) {
                names.add(name);
            }
        }
        return Set.copyOf(names);
    }

    private String elementValue(Element element) {
        return firstPresent(
            element.attr("content"),
            element.attr("title"),
            element.attr("alt"),
            element.text()
        );
    }

    private String buildChapterUrl(String pattern, String slug, int chapterNumber) {
        if (pattern.contains("{number}")) {
            return pattern.replace("{number}", Integer.toString(chapterNumber)).replace("{slug}", slug);
        }
        if (pattern.contains("{chapterNumber}")) {
            return pattern.replace("{chapterNumber}", Integer.toString(chapterNumber)).replace("{slug}", slug);
        }
        if (pattern.contains("%d")) {
            return String.format(pattern, chapterNumber);
        }
        throw new IllegalStateException("Chapter URL pattern must include {number}, {chapterNumber}, or %d.");
    }

    private int parseFirstInteger(String value) {
        Matcher matcher = Pattern.compile("\\d+").matcher(value.replace(",", ""));
        if (!matcher.find()) {
            throw new IllegalStateException("Could not parse integer from value: " + value);
        }
        return Integer.parseInt(matcher.group());
    }

    private Integer extractTrailingNumber(String url) {
        Matcher matcher = TRAILING_NUMBER_PATTERN.matcher(url);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }

    private String extractLastPathSegment(String url) {
        try {
            String path = new URI(url).getPath();
            if (path == null || path.isBlank()) {
                return "";
            }
            String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
            int slashIndex = trimmed.lastIndexOf('/');
            return slashIndex >= 0 ? trimmed.substring(slashIndex + 1) : trimmed;
        } catch (URISyntaxException exception) {
            return "";
        }
    }

    private String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\u00A0', ' ')
            .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
            .replaceAll("\\n{3,}", "\n\n")
            .trim();
    }
}
