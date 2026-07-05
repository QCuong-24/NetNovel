package com.example.netnovel_crawler.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextCleanerTest {

    // Shared crawler cleanup rules for inline text and multi-paragraph chapter content.

    @Test
    void cleanInlineReturnsEmptyStringForNull() {
        assertEquals("", TextCleaner.cleanInline(null));
    }

    @Test
    void cleanInlineNormalizesNbspAndWhitespace() {
        assertEquals("Chapter 1 The Beginning", TextCleaner.cleanInline(" Chapter\u00a01\t  The   Beginning "));
    }

    @Test
    void cleanContentReturnsEmptyStringForNull() {
        assertEquals("", TextCleaner.cleanContent(null));
    }

    @Test
    void cleanContentNormalizesLineEndingsAndCollapsesExcessBlankLines() {
        String raw = " First\t paragraph\r\n\r\n\r\nSecond\u00a0paragraph\rThird paragraph ";

        assertEquals("First paragraph\n\nSecond paragraph\nThird paragraph", TextCleaner.cleanContent(raw));
    }
}
