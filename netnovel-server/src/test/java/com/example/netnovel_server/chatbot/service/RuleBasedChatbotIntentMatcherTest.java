package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.service.language.ChatbotTextNormalizer;
import com.example.netnovel_server.chatbot.service.matching.RuleBasedChatbotIntentMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBasedChatbotIntentMatcherTest {

    private RuleBasedChatbotIntentMatcher matcher;

    @BeforeEach
    void setUp() {
        ChatbotTextNormalizer normalizer = new ChatbotTextNormalizer();
        matcher = new RuleBasedChatbotIntentMatcher(new ChatbotKnowledgeBase(), normalizer);
    }

    @Test
    void matchesVietnameseGenreAndCompletedStatus() {
        ChatbotMatchResult result = matcher.match("truyện tiên hiệp hoàn thành", ChatbotLanguage.VI);

        assertEquals("filtered_novels", result.intent(), result.toString());
        assertTrue(result.confidence() >= 0.45);
        assertEquals("Xianxia", result.filters().get("genre"), result.toString());
        assertEquals("COMPLETED", result.filters().get("status"));
    }

    @Test
    void matchesEnglishRomanceAndCompletedStatus() {
        ChatbotMatchResult result = matcher.match("completed romance novels", ChatbotLanguage.EN);

        assertEquals("filtered_novels", result.intent(), result.toString());
        assertTrue(result.confidence() >= 0.45);
        assertEquals("Romance", result.filters().get("genre"), result.toString());
        assertEquals("COMPLETED", result.filters().get("status"));
    }

    @Test
    void matchesEnglishNaturalTagSearchWithBooksLikePhrase() {
        ChatbotMatchResult result = matcher.match("I want books like cultivation", ChatbotLanguage.EN);

        assertEquals("filtered_novels", result.intent(), result.toString());
        assertTrue(result.confidence() >= 0.45);
        assertEquals("Cultivation", result.filters().get("tag"));
    }

    @Test
    void matchesVietnameseNaturalTagSearchWithKieuPhrase() {
        ChatbotMatchResult result = matcher.match("tôi muốn truyện kiểu tu luyện", ChatbotLanguage.VI);

        assertEquals("filtered_novels", result.intent(), result.toString());
        assertTrue(result.confidence() >= 0.45);
        assertEquals("Cultivation", result.filters().get("tag"));
    }

    @Test
    void matchesImportedVietnameseTagAliasesWithAndWithoutAccents() {
        ChatbotMatchResult accented = matcher.match("truyện âm mưu và mưu kế", ChatbotLanguage.VI);
        ChatbotMatchResult unaccented = matcher.match("truyen yeu to tro choi", ChatbotLanguage.VI);

        assertEquals("Schemes And Conspiracies", accented.filters().get("tag"), accented.toString());
        assertEquals("Game Elements", unaccented.filters().get("tag"), unaccented.toString());
    }

    @Test
    void mergesImportedAliasesIntoExistingTags() {
        ChatbotMatchResult result = matcher.match("tìm truyện tu tiên", ChatbotLanguage.VI);

        assertEquals("filtered_novels", result.intent(), result.toString());
        assertEquals("Cultivation", result.filters().get("tag"), result.toString());
        assertEquals(null, result.filters().get("q"), result.toString());
    }

    @Test
    void matchesNewVietnameseGenreAliasesWithAndWithoutAccents() {
        ChatbotMatchResult accented = matcher.match("truyện giả tưởng", ChatbotLanguage.VI);
        ChatbotMatchResult unaccented = matcher.match("truyen vo thuat", ChatbotLanguage.VI);

        assertEquals("Fantasy", accented.filters().get("genre"), accented.toString());
        assertEquals("Martial Arts", unaccented.filters().get("genre"), unaccented.toString());
    }

    @Test
    void matchesLongerGenreBeforeShorterGenre() {
        ChatbotMatchResult result = matcher.match("shoujo ai stories", ChatbotLanguage.EN);

        assertEquals("Shoujo Ai", result.filters().get("genre"), result.toString());
    }

    @Test
    void matchesFollowFaq() {
        ChatbotMatchResult result = matcher.match("làm sao theo dõi truyện", ChatbotLanguage.VI);

        assertEquals("faq", result.intent());
        assertNotNull(result.faq());
        assertEquals("how_to_follow_novel", result.faq().id());
    }

    @Test
    void matchesWebsiteOverviewFaq() {
        ChatbotMatchResult result = matcher.match("what features does this website have", ChatbotLanguage.EN);

        assertEquals("faq", result.intent());
        assertNotNull(result.faq());
        assertEquals("website_overview", result.faq().id());
    }

    @Test
    void matchesCollectionExplanationFaq() {
        ChatbotMatchResult result = matcher.match("collection là gì", ChatbotLanguage.VI);

        assertEquals("faq", result.intent());
        assertNotNull(result.faq());
        assertEquals("collection_feature_explanation", result.faq().id());
    }

    @Test
    void extractsAuthorSearch() {
        ChatbotMatchResult result = matcher.match("novels by Kim Dung", ChatbotLanguage.EN);

        assertEquals("filtered_novels", result.intent());
        assertEquals("kim dung", result.filters().get("author"));
    }

    @Test
    void extractsVietnameseTitleSearch() {
        ChatbotMatchResult result = matcher.match("tìm truyện có tên đấu phá", ChatbotLanguage.VI);

        assertEquals("search_by_title", result.intent());
        assertEquals("title", result.filters().get("scope"));
        assertEquals("dau pha", result.filters().get("q"));
    }

    @Test
    void extractsEnglishTitleSearch() {
        ChatbotMatchResult result = matcher.match("find novels containing lord of mysteries", ChatbotLanguage.EN);

        assertEquals("search_by_title", result.intent());
        assertEquals("title", result.filters().get("scope"));
        assertEquals("lord mysteries", result.filters().get("q"));
    }

    @Test
    void matchesCollectionNavigation() {
        ChatbotMatchResult result = matcher.match("open my collection", ChatbotLanguage.EN);

        assertEquals("navigate_collection", result.intent());
        assertNotNull(result.intentDefinition());
        assertEquals("navigation", result.intentDefinition().type());
    }

    @Test
    void matchesProfileNavigation() {
        ChatbotMatchResult result = matcher.match("mở profile", ChatbotLanguage.VI);

        assertEquals("navigate_profile", result.intent());
        assertNotNull(result.intentDefinition());
    }

    @Test
    void clarifiesAmbiguousSaveNovelRequest() {
        ChatbotMatchResult result = matcher.match("lưu truyện", ChatbotLanguage.VI);

        assertTrue(result.ambiguous());
        assertEquals("clarify_save_novel", result.intent());
    }

    @Test
    void clarifiesVagueRecommendationRequest() {
        ChatbotMatchResult result = matcher.match("truyện hay", ChatbotLanguage.VI);

        assertTrue(result.ambiguous());
        assertEquals("clarify_search", result.intent());
    }

    @Test
    void fallsBackForUnrelatedQuestion() {
        ChatbotMatchResult result = matcher.match("what is the weather tomorrow", ChatbotLanguage.EN);

        assertEquals("fallback", result.intent());
        assertEquals(0.0, result.confidence());
    }
}
