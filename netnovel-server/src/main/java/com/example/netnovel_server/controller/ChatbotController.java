package com.example.netnovel_server.controller;

import com.example.netnovel_server.chatbot.dto.ChatbotRequestDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotResponseDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotEmbeddingReindexResponseDTO;
import com.example.netnovel_server.chatbot.service.embedding.ChatbotEmbeddingReindexService;
import com.example.netnovel_server.chatbot.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot", description = "FAQ and intent chatbot APIs")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatbotEmbeddingReindexService embeddingReindexService;

    public ChatbotController(ChatbotService chatbotService, ChatbotEmbeddingReindexService embeddingReindexService) {
        this.chatbotService = chatbotService;
        this.embeddingReindexService = embeddingReindexService;
    }

    @PostMapping("/message")
    @Operation(summary = "Send a message to the FAQ and intent chatbot")
    public ResponseEntity<ChatbotResponseDTO> message(@RequestBody ChatbotRequestDTO request) {
        return ResponseEntity.ok(chatbotService.handle(request));
    }

    @PostMapping("/embeddings/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reindex chatbot FAQ and intent embeddings")
    public ResponseEntity<ChatbotEmbeddingReindexResponseDTO> reindexEmbeddings() {
        return ResponseEntity.ok(embeddingReindexService.reindex());
    }
}

