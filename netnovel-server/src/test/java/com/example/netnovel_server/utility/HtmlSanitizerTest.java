package com.example.netnovel_server.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlSanitizerTest {

    @Test
    void plainTextReturnsNullForNull() {
        assertNull(HtmlSanitizer.plainText(null));
    }

    @Test
    void plainTextRemovesHtmlTagsAndTrimsValue() {
        String sanitized = HtmlSanitizer.plainText("  <p>Hello <strong>reader</strong></p>  ");

        assertEquals("Hello reader", sanitized);
    }

    @Test
    void basicContentKeepsSafeFormattingAndRemovesUnsafeMarkup() {
        String sanitized = HtmlSanitizer.basicContent("""
            <p class="intro" style="color:red">Hello <strong>reader</strong></p>
            <script>alert('xss')</script>
            <a href="javascript:alert(1)">bad</a>
            <a href="https://example.com/novel">safe</a>
            """);

        assertTrue(sanitized.contains("<p>"));
        assertTrue(sanitized.contains("<strong>reader</strong>"));
        assertTrue(sanitized.contains("href=\"https://example.com/novel\""));
        assertFalse(sanitized.contains("class="));
        assertFalse(sanitized.contains("style="));
        assertFalse(sanitized.contains("<script>"));
        assertFalse(sanitized.contains("javascript:"));
    }

    @Test
    void basicContentReturnsNullForNull() {
        assertNull(HtmlSanitizer.basicContent(null));
    }

    @Test
    void safeUrlLikeTextRejectsDangerousSchemes() {
        assertEquals("", HtmlSanitizer.safeUrlLikeText(" javascript:alert(1) "));
        assertEquals("", HtmlSanitizer.safeUrlLikeText(" DATA:text/html,<script>alert(1)</script> "));
        assertEquals("", HtmlSanitizer.safeUrlLikeText(" vbscript:msgbox(1) "));
    }

    @Test
    void safeUrlLikeTextTrimsSafeValues() {
        assertEquals(
            "https://cdn.example.com/images/cover.jpg",
            HtmlSanitizer.safeUrlLikeText("  https://cdn.example.com/images/cover.jpg  ")
        );
    }
}
