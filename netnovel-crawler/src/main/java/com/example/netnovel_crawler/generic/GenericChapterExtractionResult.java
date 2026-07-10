package com.example.netnovel_crawler.generic;

public record GenericChapterExtractionResult(
    Integer chapterNumber,
    String sourceChapterUrl,
    String title,
    String content
) {
}
