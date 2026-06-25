package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
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

        assertTrue(result.confidence() >= 0.45);
        assertEquals("Xianxia", result.filters().get("genre"), result.toString());
        assertEquals("COMPLETED", result.filters().get("status"));
    }

    @Test
    void matchesEnglishRomanceAndCompletedStatus() {
        ChatbotMatchResult result = matcher.match("completed romance novels", ChatbotLanguage.EN);

        assertTrue(result.confidence() >= 0.45);
        assertEquals("Romance", result.filters().get("genre"), result.toString());
        assertEquals("COMPLETED", result.filters().get("status"));
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

        assertEquals("search_novel", result.intent());
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
