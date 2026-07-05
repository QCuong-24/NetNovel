package com.example.netnovel_crawler.adapter;

import com.example.netnovel_crawler.dto.CrawlNovelRequestMessage;
import com.example.netnovel_crawler.source.CrawlerEngine;
import com.example.netnovel_crawler.source.CrawlerSource;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CrawlerAdapterDispatcherTest {

    // Dispatcher chooses the adapter from source.engine(); adapters themselves stay mocked.

    private final JsoupCrawlerAdapter jsoupCrawlerAdapter = mock(JsoupCrawlerAdapter.class);
    private final PlaywrightCrawlerAdapter playwrightCrawlerAdapter = mock(PlaywrightCrawlerAdapter.class);
    private final CrawlerAdapterDispatcher dispatcher = new CrawlerAdapterDispatcher(
        jsoupCrawlerAdapter,
        playwrightCrawlerAdapter
    );

    @Test
    void crawlNovelDispatchesJsoupSourcesToJsoupAdapter() {
        CrawlerSource source = source(CrawlerEngine.JSOUP);
        CrawlNovelRequestMessage message = message();

        dispatcher.crawlNovel(source, message);

        verify(jsoupCrawlerAdapter).crawlNovel(source, message);
        verify(playwrightCrawlerAdapter, never()).crawlNovel(source, message);
    }

    @Test
    void crawlNovelDispatchesPlaywrightSourcesToPlaywrightAdapter() {
        CrawlerSource source = source(CrawlerEngine.PLAYWRIGHT);
        CrawlNovelRequestMessage message = message();

        dispatcher.crawlNovel(source, message);

        verify(playwrightCrawlerAdapter).crawlNovel(source, message);
        verify(jsoupCrawlerAdapter, never()).crawlNovel(source, message);
    }

    @Test
    void crawlNovelTreatsNonJsoupEnginesAsPlaywrightSources() {
        CrawlerSource source = source(CrawlerEngine.PLAYWRIGHT);
        CrawlNovelRequestMessage message = message();

        dispatcher.crawlNovel(source, message);

        verify(playwrightCrawlerAdapter).crawlNovel(source, message);
        verify(jsoupCrawlerAdapter, never()).crawlNovel(source, message);
    }

    private static CrawlerSource source(CrawlerEngine engine) {
        return new CrawlerSource("wuxiaworld", "https://example.com", engine);
    }

    private static CrawlNovelRequestMessage message() {
        return new CrawlNovelRequestMessage(1L, "https://example.com/novel", 2L);
    }
}
