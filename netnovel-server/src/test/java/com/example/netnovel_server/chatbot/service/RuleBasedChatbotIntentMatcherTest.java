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
        assertEquals("Tiên Hiệp", result.filters().get("genre"));
        assertEquals("COMPLETED", result.filters().get("status"));
    }

    @Test
    void matchesEnglishRomanceAndCompletedStatus() {
        ChatbotMatchResult result = matcher.match("completed romance novels", ChatbotLanguage.EN);

        assertTrue(result.confidence() >= 0.45);
        assertEquals("Ngôn Tình", result.filters().get("genre"));
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
    void extractsAuthorSearch() {
        ChatbotMatchResult result = matcher.match("novels by Kim Dung", ChatbotLanguage.EN);

        assertEquals("search_novel", result.intent());
        assertEquals("kim dung", result.filters().get("author"));
    }

    @Test
    void fallsBackForUnrelatedQuestion() {
        ChatbotMatchResult result = matcher.match("what is the weather tomorrow", ChatbotLanguage.EN);

        assertEquals("fallback", result.intent());
        assertEquals(0.0, result.confidence());
    }
}
