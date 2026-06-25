package com.example.netnovel_server.chatbot.repository;

import com.example.netnovel_server.chatbot.entity.ChatbotFallbackLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatbotFallbackLogRepository extends JpaRepository<ChatbotFallbackLog, Long> {
}
