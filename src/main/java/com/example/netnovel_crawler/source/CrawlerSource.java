package com.example.netnovel_crawler.source;

public record CrawlerSource(
    String name,
    String domain,
    CrawlerEngine engine
) {
}
