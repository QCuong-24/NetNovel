package com.example.netnovel_server.utility;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class TextUtils {

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

        return Arrays.stream(normalized.split(" "))
            .map(TextUtils::capitalizeFirstLetter)
            .collect(Collectors.joining(" "));
    }

    private static String capitalizeFirstLetter(String value) {
        if (value.isBlank()) {
            return value;
        }

        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
