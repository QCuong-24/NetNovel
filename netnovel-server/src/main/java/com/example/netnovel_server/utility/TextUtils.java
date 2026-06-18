package com.example.netnovel_server.utility;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

public final class TextUtils {

    private static final Set<String> LOWERCASE_TITLE_WORDS = Set.of("a", "an", "and", "as", "at", "but", "by", "for", "in", "nor", "of", "on", "or", "the", "to");

    private TextUtils() {
    }

    public static String toTitleCaseWords(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "";
        }

        String[] words = normalized.split(" ");

        return IntStream.range(0, words.length)
            .mapToObj(index -> shouldStayLowercase(words[index], index, words) ? words[index] : capitalizeFirstLetter(words[index]))
            .collect(Collectors.joining(" "));
    }

    private static boolean shouldStayLowercase(String word, int index, String[] words) {
        return words.length > 1
            && index > 0
            && index < words.length - 1
            && LOWERCASE_TITLE_WORDS.contains(word);
    }

    private static String capitalizeFirstLetter(String value) {
        if (value.isBlank()) {
            return value;
        }

        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
