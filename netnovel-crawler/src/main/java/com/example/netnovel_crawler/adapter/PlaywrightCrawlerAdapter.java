package com.example.netnovel_crawler.adapter;

import com.example.netnovel_crawler.dto.CrawlNovelRequestMessage;
import com.example.netnovel_crawler.source.CrawlerSource;
import org.springframework.stereotype.Component;

@Component
public class PlaywrightCrawlerAdapter implements CrawlerAdapter {

    @Override
    public void crawlNovel(CrawlerSource source, CrawlNovelRequestMessage message) {
        throw new UnsupportedOperationException("Playwright adapter is not implemented for source: " + source.name());
    }
}
