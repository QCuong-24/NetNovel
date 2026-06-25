package com.example.netnovel_server.chatbot.model;

import java.util.List;
import java.util.Map;

public record ChatbotFaq(
    String id,
    String type,
    Boolean enabled,
    Integer priority,
    Map<String, List<String>> examples,
    Map<String, String> answers,
    List<String> actionUrls,
    List<String> tags
) {
}
