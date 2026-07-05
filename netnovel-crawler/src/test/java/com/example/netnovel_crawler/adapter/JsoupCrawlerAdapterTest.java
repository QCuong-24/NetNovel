package com.example.netnovel_crawler.adapter;

import com.example.netnovel_crawler.dto.CrawlNovelRequestMessage;
import com.example.netnovel_crawler.source.CrawlerEngine;
import com.example.netnovel_crawler.source.CrawlerSource;
import com.example.netnovel_crawler.wuxiaworld.WuxiaworldJsoupCrawler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class JsoupCrawlerAdapterTest {

    // Jsoup adapter supports only named source implementations; unsupported sources fail fast.

    private final WuxiaworldJsoupCrawler wuxiaworldJsoupCrawler = mock(WuxiaworldJsoupCrawler.class);
    private final JsoupCrawlerAdapter adapter = new JsoupCrawlerAdapter(wuxiaworldJsoupCrawler);

    @Test
    void crawlNovelDelegatesWuxiaworldSourceToWuxiaworldCrawler() {
        CrawlerSource source = new CrawlerSource("wuxiaworld", "https://www.wuxiaworld.com", CrawlerEngine.JSOUP);
        CrawlNovelRequestMessage message = new CrawlNovelRequestMessage(1L, "https://example.com/novel", 2L);

        adapter.crawlNovel(source, message);

        verify(wuxiaworldJsoupCrawler).crawlNovel(source, message);
    }

    @Test
    void crawlNovelRejectsUnsupportedJsoupSource() {
        CrawlerSource source = new CrawlerSource("unknown", "https://example.com", CrawlerEngine.JSOUP);
        CrawlNovelRequestMessage message = new CrawlNovelRequestMessage(1L, "https://example.com/novel", 2L);

        assertThrows(UnsupportedOperationException.class, () -> adapter.crawlNovel(source, message));

        verify(wuxiaworldJsoupCrawler, never()).crawlNovel(source, message);
    }
}
