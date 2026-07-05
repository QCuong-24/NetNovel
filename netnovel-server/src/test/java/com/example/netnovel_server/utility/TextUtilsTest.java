package com.example.netnovel_server.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TextUtilsTest {

    // Text normalization used before persisting display names such as genres/tags.

    @Test
    void toTitleCaseWordsReturnsNullForNullValue() {
        assertNull(TextUtils.toTitleCaseWords(null));
    }

    @Test
    void toTitleCaseWordsReturnsEmptyStringForBlankValue() {
        assertEquals("", TextUtils.toTitleCaseWords("   \t  "));
    }

    @Test
    void toTitleCaseWordsNormalizesWhitespaceAndCapitalization() {
        assertEquals("Lord of the Mysteries", TextUtils.toTitleCaseWords("  LORD   OF   THE mysteries "));
    }

    @Test
    void toTitleCaseWordsKeepsSmallWordsLowercaseInsideTitle() {
        assertEquals("The Beginning and the End", TextUtils.toTitleCaseWords("the beginning and the end"));
    }
}
