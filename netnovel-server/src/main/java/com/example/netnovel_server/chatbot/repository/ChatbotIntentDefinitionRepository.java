package com.example.netnovel_server.chatbot.repository;

import com.example.netnovel_server.chatbot.entity.ChatbotIntentDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotIntentDefinitionRepository extends JpaRepository<ChatbotIntentDefinition, String> {
}
