package com.example.netnovel_server.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatbotAdminSummaryDTO {

    private long faqCount;

    private long intentCount;

    private long enabledFaqCount;

    private long enabledIntentCount;
}
