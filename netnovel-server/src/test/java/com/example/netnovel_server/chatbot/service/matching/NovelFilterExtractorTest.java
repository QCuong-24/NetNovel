package com.example.netnovel_server.chatbot.service.matching;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.service.ChatbotKnowledgeBase;
import com.example.netnovel_server.chatbot.service.language.ChatbotTextNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NovelFilterExtractorTest {

    // Novel filter extraction coverage: genre/tag/status/sort/author/title/free-query detection.

    private NovelFilterExtractor extractor;
    private ChatbotTextNormalizer normalizer;

    @BeforeEach
    void setUp() {
        ChatbotKnowledgeBase knowledgeBase = new ChatbotKnowledgeBase();
        normalizer = new ChatbotTextNormalizer();
        PhraseScorer phraseScorer = new PhraseScorer(knowledgeBase);
        ConfiguredIntentMatcher configuredIntentMatcher = new ConfiguredIntentMatcher(knowledgeBase, normalizer, phraseScorer);
        extractor = new NovelFilterExtractor(knowledgeBase, normalizer, configuredIntentMatcher);
    }

    @Test
    void extractsVietnameseGenreAndCompletedStatus() {
        ChatbotMatchResult result = match("truyện tiên hiệp hoàn thành", ChatbotLanguage.VI);

        assertEquals("filtered_novels", result.intent(), result.toString());
        assertEquals("Xianxia", result.filters().get("genre"), result.toString());
        assertEquals("COMPLETED", result.filters().get("status"), result.toString());
        assertTrue(result.confidence() >= 0.45);
    }

    @Test
    void extractsEnglishGenreAndCompletedStatus() {
        ChatbotMatchResult result = match("completed romance novels", ChatbotLanguage.EN);

        assertEquals("filtered_novels", result.intent(), result.toString());
        assertEquals("Romance", result.filters().get("genre"), result.toString());
        assertEquals("COMPLETED", result.filters().get("status"), result.toString());
    }

    @Test
    void extractsNaturalEnglishTagSearch() {
        ChatbotMatchResult result = match("I want books like cultivation", ChatbotLanguage.EN);

        assertEquals("filtered_novels", result.intent(), result.toString());
        assertEquals("Cultivation", result.filters().get("tag"), result.toString());
    }

    @Test
    void extractsVietnameseTagAliasWithDiacritics() {
        ChatbotMatchResult result = match("tìm truyện tu tiên", ChatbotLanguage.VI);

        assertEquals("filtered_novels", result.intent(), result.toString());
        assertEquals("Cultivation", result.filters().get("tag"), result.toString());
        assertEquals(null, result.filters().get("q"), result.toString());
    }

    @Test
    void extractsVietnameseTagAliasWithoutDiacritics() {
        ChatbotMatchResult result = match("truyen yeu to tro choi", ChatbotLanguage.VI);

        assertEquals("filtered_novels", result.intent(), result.toString());
        assertEquals("Game Elements", result.filters().get("tag"), result.toString());
    }

    @Test
    void extractsPopularSortIntent() {
        ChatbotMatchResult result = match("truyện hot", ChatbotLanguage.VI);

        assertEquals("popular_novels", result.intent(), result.toString());
        assertEquals("popular", result.filters().get("sort"), result.toString());
    }

    @Test
    void extractsLatestSortIntent() {
        ChatbotMatchResult result = match("latest updates", ChatbotLanguage.EN);

        assertEquals("latest_novels", result.intent(), result.toString());
        assertEquals("latest", result.filters().get("sort"), result.toString());
    }

    @Test
    void extractsAuthorSearch() {
        ChatbotMatchResult result = match("novels by Kim Dung", ChatbotLanguage.EN);

        assertEquals("filtered_novels", result.intent(), result.toString());
        assertEquals("kim dung", result.filters().get("author"), result.toString());
    }

    @Test
    void extractsVietnameseTitleSearch() {
        ChatbotMatchResult result = match("tìm truyện có tên đấu phá", ChatbotLanguage.VI);

        assertEquals("search_by_title", result.intent(), result.toString());
        assertEquals("title", result.filters().get("scope"), result.toString());
        assertEquals("dau pha", result.filters().get("q"), result.toString());
    }

    @Test
    void extractsEnglishTitleSearch() {
        ChatbotMatchResult result = match("find novels containing lord of mysteries", ChatbotLanguage.EN);

        assertEquals("search_by_title", result.intent(), result.toString());
        assertEquals("title", result.filters().get("scope"), result.toString());
        assertEquals("lord mysteries", result.filters().get("q"), result.toString());
    }

    @Test
    void fallsBackWhenNoNovelSignalOrFilterExists() {
        ChatbotMatchResult result = match("what is the weather tomorrow", ChatbotLanguage.EN);

        assertEquals("fallback", result.intent(), result.toString());
        assertEquals(0.0, result.confidence());
    }

    private ChatbotMatchResult match(String message, ChatbotLanguage language) {
        return extractor.match(normalizer.normalize(message), language);
    }
}
