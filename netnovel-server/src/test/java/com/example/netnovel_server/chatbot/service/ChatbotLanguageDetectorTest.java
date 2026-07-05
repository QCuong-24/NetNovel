package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.service.language.ChatbotLanguageDetector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatbotLanguageDetectorTest {

    // Language detection smoke coverage for short English and Vietnamese user messages.

    private final ChatbotLanguageDetector detector = new ChatbotLanguageDetector();

    @Test
    void detectsShortEnglishNovelRequest() {
        assertEquals(ChatbotLanguage.EN, detector.detect("i want to save novel", null));
    }

    @Test
    void keepsVietnameseMarkedTextAsVietnamese() {
        assertEquals(ChatbotLanguage.VI, detector.detect("tôi muốn lưu truyện", null));
    }
}
