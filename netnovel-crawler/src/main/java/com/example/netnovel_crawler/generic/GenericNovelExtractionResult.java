package com.example.netnovel_crawler.generic;

import java.util.List;
import java.util.Set;

public record GenericNovelExtractionResult(
    String sourceName,
    String sourceNovelUrl,
    String title,
    String author,
    String description,
    String coverImageUrl,
    Set<String> genres,
    Set<String> tags,
    List<GenericChapterLink> chapters
) {
}
