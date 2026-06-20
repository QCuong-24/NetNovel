package com.example.netnovel_crawler.wuxiaworld;

import org.springframework.stereotype.Component;

@Component
public class WuxiaworldProperties {

    private static final String TITLE_SELECTOR = "h5.mantine-Text-root.mantine-Title-root.mantine-1ra9ysm";
    private static final String AUTHOR_SELECTOR = "div.mantine-Text-root.mantine-ss2azu";
    private static final String DESCRIPTION_SELECTOR = "div.mantine-Text-root.mantine-tpna8b";
    private static final String TOTAL_CHAPTERS_SELECTOR = "div.mantine-Text-root.mantine-19n0k2t";
    private static final String CHAPTER_CONTENT_SELECTOR = "div#chapterText.mantine-Text-root.mantine-1ekvxsp";
    private static final String GENRE_SELECTOR = "div.mantine-Badge-root.mantine-1wmuzd5";
    private static final String TAG_SELECTOR = "div.mantine-Badge-root.mantine-6lmnus";
    private static final String COVER_IMAGE_SELECTOR = "div.mantine-Image-root.mantine-yxmaw9 img.mantine-fp9t1o.mantine-Image-image";

    public String titleSelector() {
        return TITLE_SELECTOR;
    }

    public String authorSelector() {
        return AUTHOR_SELECTOR;
    }

    public String descriptionSelector() {
        return DESCRIPTION_SELECTOR;
    }

    public String totalChaptersSelector() {
        return TOTAL_CHAPTERS_SELECTOR;
    }

    public String chapterContentSelector() {
        return CHAPTER_CONTENT_SELECTOR;
    }

    public String genreSelector() {
        return GENRE_SELECTOR;
    }

    public String tagSelector() {
        return TAG_SELECTOR;
    }

    public String coverImageSelector() {
        return COVER_IMAGE_SELECTOR;
    }
}
