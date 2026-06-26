package com.example.netnovel_server.chatbot.service.matching;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import org.springframework.stereotype.Component;

/**
 * Detects intentionally ambiguous user messages that should ask a follow-up
 * instead of jumping straight to FAQ or novel search.
 */
@Component
public class ChatbotClarificationMatcher {

    public ChatbotMatchResult match(String normalized, ChatbotLanguage language) {
        if (containsAny(normalized, "luu truyen", "luu lai", "save novel", "save story")) {
            return ChatbotMatchResult.clarify(language, "save_novel", 0.4);
        }

        if (containsAny(normalized, "truyen hay", "truyen nao hay", "good novel", "good novels", "recommend something")) {
            return ChatbotMatchResult.clarify(language, "search", 0.35);
        }

        return null;
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
