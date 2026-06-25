package com.example.netnovel_server.chatbot.service;

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

        boolean hasVietnameseMarks = normalized.matches(".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ].*");
        long asciiWordCount = normalized.replaceAll("[^a-z\\s]", " ").trim().split("\\s+").length;

        if (!hasVietnameseMarks && (englishScore >= 2 || (englishScore >= 1 && asciiWordCount >= 3))) {
            return ChatbotLanguage.EN;
        }

        return ChatbotLanguage.VI;
    }
}
