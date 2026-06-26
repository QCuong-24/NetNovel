package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.dto.ChatbotRequestDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotResponseDTO;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.service.embedding.ChatbotSemanticMatcher;
import com.example.netnovel_server.chatbot.service.language.ChatbotLanguageDetector;
import com.example.netnovel_server.chatbot.service.logging.ChatbotFallbackLogger;
import com.example.netnovel_server.chatbot.service.matching.ChatbotIntentMatcher;
import com.example.netnovel_server.chatbot.service.response.ChatbotResponseFactory;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private final ChatbotLanguageDetector languageDetector;
    private final ChatbotIntentMatcher intentMatcher;
    private final ChatbotFallbackLogger fallbackLogger;
    private final ChatbotSemanticMatcher semanticMatcher;
    private final ChatbotResponseFactory responseFactory;

    public ChatbotService(
        ChatbotLanguageDetector languageDetector,
        ChatbotIntentMatcher intentMatcher,
        ChatbotFallbackLogger fallbackLogger,
        ChatbotSemanticMatcher semanticMatcher,
        ChatbotResponseFactory responseFactory
    ) {
        this.languageDetector = languageDetector;
        this.intentMatcher = intentMatcher;
        this.fallbackLogger = fallbackLogger;
        this.semanticMatcher = semanticMatcher;
        this.responseFactory = responseFactory;
    }

    public ChatbotResponseDTO handle(ChatbotRequestDTO request) {
        String message = request != null ? request.getMessage() : "";
        ChatbotLanguage language = languageDetector.detect(message, request != null ? request.getLanguage() : null);
        ChatbotMatchResult match = intentMatcher.match(message, language);

        if (shouldTrySemantic(match)) {
            match = semanticMatcher.match(message, language).orElse(match);
        }

        if (match.ambiguous()) {
            fallbackLogger.log(message, language, match.intent(), match.confidence(), match.filters(), null);
            return responseFactory.clarificationResponse(match);
        }

        if ("faq".equals(match.intent()) && match.faq() != null) {
            return responseFactory.faqResponse(match);
        }

        if (match.intentDefinition() != null && "navigation".equals(match.intentDefinition().type())) {
            return responseFactory.navigationResponse(match);
        }

        if (match.confidence() >= 0.45 && match.intent().contains("novel")) {
            ChatbotResponseDTO response = responseFactory.novelSearchResponse(match);
            if (response.getNovels().isEmpty()) {
                fallbackLogger.log(message, language, match.intent(), match.confidence(), match.filters(), 0);
            }
            return response;
        }

        fallbackLogger.log(message, language, match.intent(), match.confidence(), match.filters(), null);
        return responseFactory.fallbackResponse(language);
    }

    private boolean shouldTrySemantic(ChatbotMatchResult match) {
        if (match.ambiguous()) {
            return true;
        }
        if ("fallback".equals(match.intent())) {
            return true;
        }
        if ("faq".equals(match.intent()) && match.confidence() < 0.7) {
            return true;
        }
        return match.confidence() < 0.55 && match.filters().isEmpty();
    }

}
