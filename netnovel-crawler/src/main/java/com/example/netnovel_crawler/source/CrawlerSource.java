package com.example.netnovel_crawler.source;

public record CrawlerSource(
    String name,
    String domain,
    CrawlerEngine engine,
    Long profileId
) {

    public CrawlerSource(String name, String domain, CrawlerEngine engine) {
        this(name, domain, engine, null);
    }

    public boolean dynamic() {
        return profileId != null;
    }
}
