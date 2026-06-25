package com.example.netnovel_server.chatbot.model;

import java.util.List;
import java.util.Map;

public record ChatbotIntent(
    String id,
    String type,
    Boolean enabled,
    Integer priority,
    Map<String, List<String>> examples,
    Map<String, String> replies,
    Map<String, String> filters,
    List<String> tags,
    List<ChatbotIntentAction> actions
) {
}
