package com.example.netnovel_crawler.adapter;

import com.example.netnovel_crawler.dto.CrawlNovelRequestMessage;
import com.example.netnovel_crawler.source.CrawlerEngine;
import com.example.netnovel_crawler.source.CrawlerSource;
import org.springframework.stereotype.Component;

@Component
public class CrawlerAdapterDispatcher {

    private final JsoupCrawlerAdapter jsoupCrawlerAdapter;
    private final PlaywrightCrawlerAdapter playwrightCrawlerAdapter;

    public CrawlerAdapterDispatcher(
        JsoupCrawlerAdapter jsoupCrawlerAdapter,
        PlaywrightCrawlerAdapter playwrightCrawlerAdapter
    ) {
        this.jsoupCrawlerAdapter = jsoupCrawlerAdapter;
        this.playwrightCrawlerAdapter = playwrightCrawlerAdapter;
    }

    public void crawlNovel(CrawlerSource source, CrawlNovelRequestMessage message) {
        if (source.engine() == CrawlerEngine.JSOUP) {
            jsoupCrawlerAdapter.crawlNovel(source, message);
            return;
        }

        playwrightCrawlerAdapter.crawlNovel(source, message);
    }
}
