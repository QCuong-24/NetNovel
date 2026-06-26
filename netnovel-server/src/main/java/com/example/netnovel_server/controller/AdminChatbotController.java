package com.example.netnovel_server.controller;

import com.example.netnovel_server.chatbot.dto.ChatbotAdminSummaryDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotEmbeddingReindexResponseDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotEmbeddingStatusDTO;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.service.embedding.ChatbotEmbeddingReindexService;
import com.example.netnovel_server.chatbot.service.embedding.ChatbotEmbeddingStatusService;
import com.example.netnovel_server.chatbot.service.ChatbotKnowledgeAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/chatbot")
@Tag(name = "Admin Chatbot", description = "Admin-only chatbot knowledge management APIs")
public class AdminChatbotController {

    private final ChatbotKnowledgeAdminService adminService;
    private final ChatbotEmbeddingReindexService embeddingReindexService;
    private final ChatbotEmbeddingStatusService embeddingStatusService;

    public AdminChatbotController(
        ChatbotKnowledgeAdminService adminService,
        ChatbotEmbeddingReindexService embeddingReindexService,
        ChatbotEmbeddingStatusService embeddingStatusService
    ) {
        this.adminService = adminService;
        this.embeddingReindexService = embeddingReindexService;
        this.embeddingStatusService = embeddingStatusService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get chatbot knowledge summary")
    public ResponseEntity<ChatbotAdminSummaryDTO> summary() {
        return ResponseEntity.ok(adminService.summary());
    }

    @PostMapping("/reload")
    @Operation(summary = "Reload chatbot knowledge cache from database")
    public ResponseEntity<ChatbotAdminSummaryDTO> reload() {
        return ResponseEntity.ok(adminService.reload());
    }

    @PostMapping("/import-defaults")
    @Operation(summary = "Import default chatbot knowledge from JSON resources")
    public ResponseEntity<ChatbotAdminSummaryDTO> importDefaults(
        @RequestParam(defaultValue = "false") boolean replaceExisting
    ) {
        return ResponseEntity.ok(adminService.importDefaults(replaceExisting));
    }

    @PostMapping("/embeddings/reindex")
    @Operation(summary = "Reindex chatbot FAQ and intent embeddings")
    public ResponseEntity<ChatbotEmbeddingReindexResponseDTO> reindexEmbeddings() {
        return ResponseEntity.ok(embeddingReindexService.reindex());
    }

    @GetMapping("/embeddings/status")
    @Operation(summary = "Get chatbot embedding index status")
    public ResponseEntity<ChatbotEmbeddingStatusDTO> embeddingStatus() {
        return ResponseEntity.ok(embeddingStatusService.status());
    }

    @GetMapping("/faqs")
    @Operation(summary = "List chatbot FAQs")
    public ResponseEntity<List<ChatbotFaq>> listFaqs() {
        return ResponseEntity.ok(adminService.listFaqs());
    }

    @GetMapping("/faqs/{id}")
    @Operation(summary = "Get chatbot FAQ")
    public ResponseEntity<ChatbotFaq> getFaq(@PathVariable String id) {
        return ResponseEntity.ok(adminService.getFaq(id));
    }

    @PutMapping("/faqs/{id}")
    @Operation(summary = "Create or update chatbot FAQ")
    public ResponseEntity<ChatbotFaq> saveFaq(@PathVariable String id, @RequestBody ChatbotFaq request) {
        ChatbotFaq faq = new ChatbotFaq(
            id,
            request.type(),
            request.enabled(),
            request.priority(),
            request.examples(),
            request.answers(),
            request.actionUrls(),
            request.tags()
        );
        return ResponseEntity.ok(adminService.saveFaq(faq));
    }

    @DeleteMapping("/faqs/{id}")
    @Operation(summary = "Delete chatbot FAQ")
    public ResponseEntity<Void> deleteFaq(@PathVariable String id) {
        adminService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/intents")
    @Operation(summary = "List chatbot intents")
    public ResponseEntity<List<ChatbotIntent>> listIntents() {
        return ResponseEntity.ok(adminService.listIntents());
    }

    @GetMapping("/intents/{id}")
    @Operation(summary = "Get chatbot intent")
    public ResponseEntity<ChatbotIntent> getIntent(@PathVariable String id) {
        return ResponseEntity.ok(adminService.getIntent(id));
    }

    @PutMapping("/intents/{id}")
    @Operation(summary = "Create or update chatbot intent")
    public ResponseEntity<ChatbotIntent> saveIntent(@PathVariable String id, @RequestBody ChatbotIntent request) {
        ChatbotIntent intent = new ChatbotIntent(
            id,
            request.type(),
            request.enabled(),
            request.priority(),
            request.examples(),
            request.replies(),
            request.filters(),
            request.tags(),
            request.actions()
        );
        return ResponseEntity.ok(adminService.saveIntent(intent));
    }

    @DeleteMapping("/intents/{id}")
    @Operation(summary = "Delete chatbot intent")
    public ResponseEntity<Void> deleteIntent(@PathVariable String id) {
        adminService.deleteIntent(id);
        return ResponseEntity.noContent().build();
    }
}

