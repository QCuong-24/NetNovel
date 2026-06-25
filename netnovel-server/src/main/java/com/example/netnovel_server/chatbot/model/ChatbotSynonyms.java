package com.example.netnovel_server.chatbot.model;

import java.util.List;
import java.util.Map;

public record ChatbotSynonyms(
    Map<String, List<String>> genres,
    Map<String, List<String>> statuses,
    Map<String, List<String>> sorts,
    Map<String, List<String>> tags,
    Map<String, List<String>> searchWords,
    Map<String, List<String>> authorWords,
    Map<String, List<String>> titleWords,
    Map<String, List<String>> stopwords
) {
}
