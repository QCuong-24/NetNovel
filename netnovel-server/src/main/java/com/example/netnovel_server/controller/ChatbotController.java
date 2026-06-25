package com.example.netnovel_server.controller;

import com.example.netnovel_server.chatbot.dto.ChatbotRequestDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotResponseDTO;
import com.example.netnovel_server.chatbot.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot", description = "FAQ and intent chatbot APIs")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    @Operation(summary = "Send a message to the FAQ and intent chatbot")
    public ResponseEntity<ChatbotResponseDTO> message(@RequestBody ChatbotRequestDTO request) {
        return ResponseEntity.ok(chatbotService.handle(request));
    }
}
