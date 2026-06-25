package com.example.netnovel_server.chatbot.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class ChatbotTextNormalizer {

    public String normalize(String value) {
        if (value == null) {
            return "";
        }

        String lower = value.toLowerCase(Locale.ROOT).trim();
        String withoutMarks = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replace('đ', 'd');

        return withoutMarks
            .replaceAll("[^a-z0-9\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }
}
