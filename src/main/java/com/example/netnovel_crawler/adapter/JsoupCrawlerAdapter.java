package com.example.netnovel_crawler.adapter;

import com.example.netnovel_crawler.dto.CrawlNovelRequestMessage;
import com.example.netnovel_crawler.source.CrawlerSource;
import com.example.netnovel_crawler.wuxiaworld.WuxiaworldJsoupCrawler;
import org.springframework.stereotype.Component;

@Component
public class JsoupCrawlerAdapter implements CrawlerAdapter {

    private final WuxiaworldJsoupCrawler wuxiaworldJsoupCrawler;

    public JsoupCrawlerAdapter(WuxiaworldJsoupCrawler wuxiaworldJsoupCrawler) {
        this.wuxiaworldJsoupCrawler = wuxiaworldJsoupCrawler;
    }

    @Override
    public void crawlNovel(CrawlerSource source, CrawlNovelRequestMessage message) {
        if ("wuxiaworld".equalsIgnoreCase(source.name())) {
            wuxiaworldJsoupCrawler.crawlNovel(source, message);
            return;
        }

        throw new UnsupportedOperationException("Jsoup adapter is not implemented for source: " + source.name());
    }
}
