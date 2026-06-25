package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class ChatbotLanguageDetector {

    private static final Set<String> ENGLISH_HINTS = Set.of(
        "how", "what", "where", "when", "why", "help", "show", "find", "search",
        "novel", "novels", "completed", "popular", "latest", "follow", "bookmark"
    );

    public ChatbotLanguage detect(String message, String requestedLanguage) {
        if ("en".equalsIgnoreCase(requestedLanguage)) {
            return ChatbotLanguage.EN;
        }
        if ("vi".equalsIgnoreCase(requestedLanguage)) {
            return ChatbotLanguage.VI;
        }

        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
        long englishScore = ENGLISH_HINTS.stream()
            .filter(word -> normalized.matches(".*\\b" + word + "\\b.*"))
            .count();

        boolean hasVietnameseMarks = normalized.matches(".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ].*");

        if (englishScore >= 2 && !hasVietnameseMarks) {
            return ChatbotLanguage.EN;
        }

        return ChatbotLanguage.VI;
    }
}
