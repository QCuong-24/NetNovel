package com.example.netnovel_server.chatbot.service.response;

import java.util.Map;

public record ChatbotSearchAccessDecision(
    boolean advancedRequired,
    boolean advancedAllowed,
    Map<String, String> searchFilters,
    Map<String, String> actionFilters
) {

    public boolean publicFallback() {
        return advancedRequired && !advancedAllowed;
    }

    public boolean advancedMode() {
        return advancedRequired && advancedAllowed;
    }
}
