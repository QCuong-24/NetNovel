package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.entity.ChatbotFallbackLog;
import com.example.netnovel_server.chatbot.repository.ChatbotFallbackLogRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChatbotFallbackLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatbotFallbackLogger.class);

    private final ChatbotFallbackLogRepository fallbackLogRepository;
    private final ChatbotTextNormalizer normalizer;
    private final ObjectMapper objectMapper;

    public ChatbotFallbackLogger(ChatbotFallbackLogRepository fallbackLogRepository, ChatbotTextNormalizer normalizer) {
        this.fallbackLogRepository = fallbackLogRepository;
        this.normalizer = normalizer;
        this.objectMapper = new ObjectMapper();
    }

    public void log(
        String message,
        ChatbotLanguage language,
        String matchedIntent,
        double confidence,
        Object filters,
        Integer novelResultCount
    ) {
        LOGGER.info(
            "Chatbot fallback language={} intent={} confidence={} resultCount={} message={}",
            language.code(),
            matchedIntent,
            confidence,
            novelResultCount,
            message
        );

        if (message == null || message.isBlank()) {
            return;
        }

        fallbackLogRepository.save(ChatbotFallbackLog.builder()
            .userId(SecurityUtils.getCurrentUserId().orElse(null))
            .message(message)
            .normalizedMessage(normalizer.normalize(message))
            .detectedLanguage(language.code())
            .matchedIntent(matchedIntent)
            .confidence(confidence)
            .filtersJson(toJson(filters))
            .novelResultCount(novelResultCount)
            .build());
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            LOGGER.warn("Could not serialize chatbot fallback filters", exception);
            return null;
        }
    }
}
