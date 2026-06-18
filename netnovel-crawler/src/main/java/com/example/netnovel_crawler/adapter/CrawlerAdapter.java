package com.example.netnovel_crawler.adapter;

import com.example.netnovel_crawler.dto.CrawlNovelRequestMessage;
import com.example.netnovel_crawler.source.CrawlerSource;

public interface CrawlerAdapter {

    void crawlNovel(CrawlerSource source, CrawlNovelRequestMessage message);
}
