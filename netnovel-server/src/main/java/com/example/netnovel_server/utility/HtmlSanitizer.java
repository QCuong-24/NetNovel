package com.example.netnovel_server.utility;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.Locale;

public final class HtmlSanitizer {

    private static final Safelist PLAIN_TEXT = Safelist.none();

    private static final Safelist BASIC_CONTENT = Safelist.basic()
        .addTags("p", "br", "ul", "ol", "li", "blockquote")
        .removeAttributes(":all", "style", "class", "id")
        .removeProtocols("a", "href", "javascript", "data", "vbscript");

    private HtmlSanitizer() {
    }

    public static String plainText(String value) {
        if (value == null) {
            return null;
        }
        return Jsoup.clean(value.trim(), PLAIN_TEXT).trim();
    }

    public static String basicContent(String value) {
        if (value == null) {
            return null;
        }
        return Jsoup.clean(value.trim(), BASIC_CONTENT).trim();
    }

    public static String safeUrlLikeText(String value) {
        String sanitized = plainText(value);
        if (sanitized == null || sanitized.isBlank()) {
            return sanitized;
        }

        String normalized = sanitized.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("javascript:")
            || normalized.startsWith("data:")
            || normalized.startsWith("vbscript:")) {
            return "";
        }
        return sanitized.trim();
    }
}
