package com.example.netnovel_server.chatbot.service.language;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class ChatbotLanguageDetector {

    private static final Set<String> ENGLISH_HINTS = Set.of(
        "i", "me", "my", "want", "need", "can", "how", "what", "where", "when", "why",
        "help", "show", "find", "search", "save", "open", "go", "read", "continue",
        "novel", "novels", "story", "stories", "completed", "popular", "latest",
        "follow", "bookmark", "collection", "profile", "dashboard", "login"
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

        boolean hasVietnameseMarks = normalized.matches(".*[\\u00e0\\u00e1\\u1ea1\\u1ea3\\u00e3\\u00e2\\u1ea7\\u1ea5\\u1ead\\u1ea9\\u1eab\\u0103\\u1eb1\\u1eaf\\u1eb7\\u1eb3\\u1eb5\\u00e8\\u00e9\\u1eb9\\u1ebb\\u1ebd\\u00ea\\u1ec1\\u1ebf\\u1ec7\\u1ec3\\u1ec5\\u00ec\\u00ed\\u1ecb\\u1ec9\\u0129\\u00f2\\u00f3\\u1ecd\\u1ecf\\u00f5\\u00f4\\u1ed3\\u1ed1\\u1ed9\\u1ed5\\u1ed7\\u01a1\\u1edd\\u1edb\\u1ee3\\u1edf\\u1ee1\\u00f9\\u00fa\\u1ee5\\u1ee7\\u0169\\u01b0\\u1eeb\\u1ee9\\u1ef1\\u1eed\\u1eef\\u1ef3\\u00fd\\u1ef5\\u1ef7\\u1ef9\\u0111].*");
        long asciiWordCount = normalized.replaceAll("[^a-z\\s]", " ").trim().split("\\s+").length;

        if (!hasVietnameseMarks && (englishScore >= 2 || (englishScore >= 1 && asciiWordCount >= 3))) {
            return ChatbotLanguage.EN;
        }

        return ChatbotLanguage.VI;
    }
}

