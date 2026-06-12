package com.example.netnovel_crawler.wuxiaworld;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WuxiaworldProperties {

    private final String titleSelector;
    private final String authorSelector;
    private final String descriptionSelector;
    private final String totalChaptersSelector;
    private final String chapterContentSelector;

    public WuxiaworldProperties(
        @Value("${app.crawler.wuxiaworld.title-selector:h5.mantine-Text-root.mantine-Title-root.mantine-1ra9ysm}") String titleSelector,
        @Value("${app.crawler.wuxiaworld.author-selector:div.mantine-Text-root.mantine-ss2azu}") String authorSelector,
        @Value("${app.crawler.wuxiaworld.description-selector:div.mantine-Text-root.mantine-tpna8b}") String descriptionSelector,
        @Value("${app.crawler.wuxiaworld.total-chapters-selector:div.mantine-Text-root.mantine-19n0k2t}") String totalChaptersSelector,
        @Value("${app.crawler.wuxiaworld.chapter-content-selector:div#chapterText.mantine-Text-root .mantine-1ekvxsp}") String chapterContentSelector
    ) {
        this.titleSelector = titleSelector;
        this.authorSelector = authorSelector;
        this.descriptionSelector = descriptionSelector;
        this.totalChaptersSelector = totalChaptersSelector;
        this.chapterContentSelector = chapterContentSelector;
    }

    public String titleSelector() {
        return titleSelector;
    }

    public String authorSelector() {
        return authorSelector;
    }

    public String descriptionSelector() {
        return descriptionSelector;
    }

    public String totalChaptersSelector() {
        return totalChaptersSelector;
    }

    public String chapterContentSelector() {
        return chapterContentSelector;
    }
}
