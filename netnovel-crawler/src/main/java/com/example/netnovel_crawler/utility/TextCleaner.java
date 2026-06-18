package com.example.netnovel_crawler.utility;

public final class TextCleaner {

    private TextCleaner() {
    }

    public static String cleanInline(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\u00a0', ' ')
            .replaceAll("\\s+", " ")
            .trim();
    }

    public static String cleanContent(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r\n", "\n")
            .replace('\r', '\n')
            .replace('\u00a0', ' ')
            .replaceAll("[ \\t]+", " ")
            .replaceAll("\\n{3,}", "\n\n")
            .trim();
    }
}
