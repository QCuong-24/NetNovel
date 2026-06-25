package com.example.netnovel_server.chatbot.model;

import java.util.HashMap;
import java.util.Map;

public record ChatbotMatchResult(
    String intent,
    ChatbotLanguage language,
    double confidence,
    double secondBestConfidence,
    boolean ambiguous,
    String clarificationType,
    Map<String, String> filters,
    ChatbotFaq faq,
    ChatbotIntent intentDefinition
) {

    public static ChatbotMatchResult fallback(ChatbotLanguage language) {
        return new ChatbotMatchResult("fallback", language, 0.0, 0.0, false, null, new HashMap<>(), null, null);
    }

    public static ChatbotMatchResult clarify(ChatbotLanguage language, String clarificationType, double confidence) {
        return new ChatbotMatchResult(
            "clarify_" + clarificationType,
            language,
            confidence,
            0.0,
            true,
            clarificationType,
            new HashMap<>(),
            null,
            null
        );
    }
}
