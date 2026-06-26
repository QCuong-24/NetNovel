package com.example.netnovel_server.chatbot.service.embedding;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class ChatbotEmbeddingVectorFormatter {

    private ChatbotEmbeddingVectorFormatter() {
    }

    public static String toPgVector(List<Double> embedding) {
        return embedding.stream()
            .map(value -> String.format(Locale.US, "%.8f", value))
            .collect(Collectors.joining(",", "[", "]"));
    }
}

