package com.example.netnovel_server.chatbot.model;

import java.util.List;
import java.util.Map;

public record ChatbotIntentAction(
    Map<String, String> labels,
    String type,
    String value,
    List<String> requiredRoles
) {
}
