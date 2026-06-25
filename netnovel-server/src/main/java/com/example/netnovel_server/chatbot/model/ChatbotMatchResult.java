package com.example.netnovel_server.chatbot.model;

import java.util.HashMap;
import java.util.Map;

public record ChatbotMatchResult(
    String intent,
    ChatbotLanguage language,
    double confidence,
    Map<String, String> filters,
    ChatbotFaq faq
) {

    public static ChatbotMatchResult fallback(ChatbotLanguage language) {
        return new ChatbotMatchResult("fallback", language, 0.0, new HashMap<>(), null);
    }
}
