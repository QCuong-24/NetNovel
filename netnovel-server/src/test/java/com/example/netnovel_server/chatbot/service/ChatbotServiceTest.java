package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.dto.ChatbotRequestDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotResponseDTO;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.service.embedding.ChatbotSemanticMatcher;
import com.example.netnovel_server.chatbot.service.language.ChatbotLanguageDetector;
import com.example.netnovel_server.chatbot.service.logging.ChatbotFallbackLogger;
import com.example.netnovel_server.chatbot.service.matching.ChatbotIntentMatcher;
import com.example.netnovel_server.chatbot.service.response.ChatbotResponseFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    // ChatbotService flow coverage: language -> matcher -> optional semantic -> response factory -> fallback logging.

    @Mock
    private ChatbotLanguageDetector languageDetector;

    @Mock
    private ChatbotIntentMatcher intentMatcher;

    @Mock
    private ChatbotFallbackLogger fallbackLogger;

    @Mock
    private ChatbotSemanticMatcher semanticMatcher;

    @Mock
    private ChatbotResponseFactory responseFactory;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = new ChatbotService(
            languageDetector,
            intentMatcher,
            fallbackLogger,
            semanticMatcher,
            responseFactory
        );
    }

    @Test
    void returnsFaqResponseWithoutFallbackLogging() {
        ChatbotRequestDTO request = request("how to follow novel", null);
        ChatbotFaq faq = new ChatbotFaq("how_to_follow_novel", "faq", true, 1, Map.of(), Map.of(), List.of(), List.of());
        ChatbotMatchResult match = new ChatbotMatchResult("faq", ChatbotLanguage.EN, 0.9, 0.0, false, null, Map.of(), faq, null);
        ChatbotResponseDTO expected = response("faq");

        when(languageDetector.detect("how to follow novel", null)).thenReturn(ChatbotLanguage.EN);
        when(intentMatcher.match("how to follow novel", ChatbotLanguage.EN)).thenReturn(match);
        when(responseFactory.faqResponse(match)).thenReturn(expected);

        ChatbotResponseDTO actual = chatbotService.handle(request);

        assertEquals(expected, actual);
        verify(responseFactory).faqResponse(match);
        verify(semanticMatcher, never()).match(any(), any());
        verify(fallbackLogger, never()).log(any(), any(), any(), any(Double.class), any(), any());
    }

    @Test
    void usesSemanticMatchWhenRuleBasedMatchFallsBack() {
        ChatbotRequestDTO request = request("what is bookmark", null);
        ChatbotFaq faq = new ChatbotFaq("bookmark_explanation", "faq", true, 1, Map.of(), Map.of(), List.of(), List.of());
        ChatbotMatchResult fallback = ChatbotMatchResult.fallback(ChatbotLanguage.EN);
        ChatbotMatchResult semanticFaq = new ChatbotMatchResult("faq", ChatbotLanguage.EN, 0.86, 0.0, false, null, Map.of(), faq, null);
        ChatbotResponseDTO expected = response("faq");

        when(languageDetector.detect("what is bookmark", null)).thenReturn(ChatbotLanguage.EN);
        when(intentMatcher.match("what is bookmark", ChatbotLanguage.EN)).thenReturn(fallback);
        when(semanticMatcher.match("what is bookmark", ChatbotLanguage.EN)).thenReturn(Optional.of(semanticFaq));
        when(responseFactory.faqResponse(semanticFaq)).thenReturn(expected);

        ChatbotResponseDTO actual = chatbotService.handle(request);

        assertEquals(expected, actual);
        verify(semanticMatcher).match("what is bookmark", ChatbotLanguage.EN);
        verify(responseFactory).faqResponse(semanticFaq);
        verify(fallbackLogger, never()).log(any(), any(), any(), any(Double.class), any(), any());
    }

    @Test
    void logsWhenNovelSearchReturnsEmptyResults() {
        ChatbotRequestDTO request = request("tìm truyện tu tiên", "vi");
        ChatbotMatchResult match = new ChatbotMatchResult(
            "filtered_novels",
            ChatbotLanguage.VI,
            0.75,
            0.0,
            false,
            null,
            Map.of("tag", "Cultivation"),
            null,
            null
        );
        ChatbotResponseDTO expected = response("filtered_novels");
        expected.setNovels(List.of());

        when(languageDetector.detect("tìm truyện tu tiên", "vi")).thenReturn(ChatbotLanguage.VI);
        when(intentMatcher.match("tìm truyện tu tiên", ChatbotLanguage.VI)).thenReturn(match);
        when(responseFactory.novelSearchResponse(match)).thenReturn(expected);

        ChatbotResponseDTO actual = chatbotService.handle(request);

        assertEquals(expected, actual);
        verify(responseFactory).novelSearchResponse(match);
        verify(fallbackLogger).log("tìm truyện tu tiên", ChatbotLanguage.VI, "filtered_novels", 0.75, match.filters(), 0);
    }

    @Test
    void returnsClarificationResponseAndLogsAmbiguousMatch() {
        ChatbotRequestDTO request = request("lưu truyện", "vi");
        ChatbotMatchResult match = ChatbotMatchResult.clarify(ChatbotLanguage.VI, "save_novel", 0.4);
        ChatbotResponseDTO expected = response("clarify_save_novel");

        when(languageDetector.detect("lưu truyện", "vi")).thenReturn(ChatbotLanguage.VI);
        when(intentMatcher.match("lưu truyện", ChatbotLanguage.VI)).thenReturn(match);
        when(semanticMatcher.match("lưu truyện", ChatbotLanguage.VI)).thenReturn(Optional.empty());
        when(responseFactory.clarificationResponse(match)).thenReturn(expected);

        ChatbotResponseDTO actual = chatbotService.handle(request);

        assertEquals(expected, actual);
        verify(fallbackLogger).log("lưu truyện", ChatbotLanguage.VI, "clarify_save_novel", 0.4, match.filters(), null);
        verify(responseFactory).clarificationResponse(match);
    }

    @Test
    void logsAndReturnsFallbackResponseWhenNothingMatches() {
        ChatbotRequestDTO request = request("what is the weather tomorrow", null);
        ChatbotMatchResult match = ChatbotMatchResult.fallback(ChatbotLanguage.EN);
        ChatbotResponseDTO expected = response("fallback");

        when(languageDetector.detect("what is the weather tomorrow", null)).thenReturn(ChatbotLanguage.EN);
        when(intentMatcher.match("what is the weather tomorrow", ChatbotLanguage.EN)).thenReturn(match);
        when(semanticMatcher.match("what is the weather tomorrow", ChatbotLanguage.EN)).thenReturn(Optional.empty());
        when(responseFactory.fallbackResponse(ChatbotLanguage.EN)).thenReturn(expected);

        ChatbotResponseDTO actual = chatbotService.handle(request);

        assertEquals(expected, actual);
        verify(fallbackLogger).log("what is the weather tomorrow", ChatbotLanguage.EN, "fallback", 0.0, match.filters(), null);
        verify(responseFactory).fallbackResponse(ChatbotLanguage.EN);
    }

    @Test
    void returnsNavigationResponseForNavigationIntent() {
        ChatbotRequestDTO request = request("open my collection", null);
        ChatbotIntent intent = new ChatbotIntent("navigate_collection", "navigation", true, 1, Map.of(), Map.of(), Map.of(), List.of(), List.of());
        ChatbotMatchResult match = new ChatbotMatchResult("navigate_collection", ChatbotLanguage.EN, 0.9, 0.0, false, null, Map.of(), null, intent);
        ChatbotResponseDTO expected = response("navigate_collection");

        when(languageDetector.detect("open my collection", null)).thenReturn(ChatbotLanguage.EN);
        when(intentMatcher.match("open my collection", ChatbotLanguage.EN)).thenReturn(match);
        when(responseFactory.navigationResponse(match)).thenReturn(expected);

        ChatbotResponseDTO actual = chatbotService.handle(request);

        assertEquals(expected, actual);
        verify(responseFactory).navigationResponse(match);
        verify(fallbackLogger, never()).log(any(), any(), any(), any(Double.class), any(), any());
    }

    private ChatbotRequestDTO request(String message, String language) {
        ChatbotRequestDTO request = new ChatbotRequestDTO();
        request.setMessage(message);
        request.setLanguage(language);
        return request;
    }

    private ChatbotResponseDTO response(String intent) {
        return ChatbotResponseDTO.builder()
            .reply("reply")
            .language("en")
            .intent(intent)
            .confidence(0.9)
            .novels(List.of())
            .suggestedQuestions(List.of())
            .actions(List.of())
            .build();
    }
}
