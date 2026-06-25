package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;

public interface ChatbotIntentMatcher {

    ChatbotMatchResult match(String message, ChatbotLanguage language);
}
