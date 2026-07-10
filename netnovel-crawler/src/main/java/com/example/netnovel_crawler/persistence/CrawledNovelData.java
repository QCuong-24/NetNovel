package com.example.netnovel_crawler.persistence;

import java.util.Set;

public record CrawledNovelData(
    String sourceName,
    String sourceDomain,
    String sourceNovelUrl,
    String externalId,
    String title,
    String author,
    String description,
    String coverImageUrl,
    Set<String> genres,
    Set<String> tags
) {
}
