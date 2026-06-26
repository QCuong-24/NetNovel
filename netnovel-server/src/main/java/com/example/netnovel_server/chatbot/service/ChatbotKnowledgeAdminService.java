package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.dto.ChatbotAdminSummaryDTO;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.repository.ChatbotFaqDefinitionRepository;
import com.example.netnovel_server.chatbot.repository.ChatbotIntentDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ChatbotKnowledgeAdminService {

    private final ChatbotKnowledgeBase knowledgeBase;
    private final ChatbotFaqDefinitionRepository faqRepository;
    private final ChatbotIntentDefinitionRepository intentRepository;

    public ChatbotKnowledgeAdminService(
        ChatbotKnowledgeBase knowledgeBase,
        ChatbotFaqDefinitionRepository faqRepository,
        ChatbotIntentDefinitionRepository intentRepository
    ) {
        this.knowledgeBase = knowledgeBase;
        this.faqRepository = faqRepository;
        this.intentRepository = intentRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatbotFaq> listFaqs() {
        return faqRepository.findAll().stream()
            .map(knowledgeBase::toModel)
            .sorted(Comparator.comparing((ChatbotFaq faq) -> faq.priority() == null ? 0 : faq.priority()).reversed())
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatbotIntent> listIntents() {
        return intentRepository.findAll().stream()
            .map(knowledgeBase::toModel)
            .sorted(Comparator.comparing((ChatbotIntent intent) -> intent.priority() == null ? 0 : intent.priority()).reversed())
            .toList();
    }

    @Transactional(readOnly = true)
    public ChatbotFaq getFaq(String id) {
        return faqRepository.findById(id)
            .map(knowledgeBase::toModel)
            .orElseThrow(() -> new NoSuchElementException("Chatbot FAQ not found: " + id));
    }

    @Transactional(readOnly = true)
    public ChatbotIntent getIntent(String id) {
        return intentRepository.findById(id)
            .map(knowledgeBase::toModel)
            .orElseThrow(() -> new NoSuchElementException("Chatbot intent not found: " + id));
    }

    @Transactional
    public ChatbotFaq saveFaq(ChatbotFaq faq) {
        ChatbotFaq saved = knowledgeBase.toModel(faqRepository.save(knowledgeBase.toEntity(faq)));
        knowledgeBase.reload();
        return saved;
    }

    @Transactional
    public ChatbotIntent saveIntent(ChatbotIntent intent) {
        ChatbotIntent saved = knowledgeBase.toModel(intentRepository.save(knowledgeBase.toEntity(intent)));
        knowledgeBase.reload();
        return saved;
    }

    @Transactional
    public void deleteFaq(String id) {
        faqRepository.deleteById(id);
        knowledgeBase.reload();
    }

    @Transactional
    public void deleteIntent(String id) {
        intentRepository.deleteById(id);
        knowledgeBase.reload();
    }

    @Transactional
    public ChatbotAdminSummaryDTO importDefaults(boolean replaceExisting) {
        knowledgeBase.importDefaults(replaceExisting);
        return summary();
    }

    public ChatbotAdminSummaryDTO reload() {
        knowledgeBase.reload();
        return summary();
    }

    @Transactional(readOnly = true)
    public ChatbotAdminSummaryDTO summary() {
        List<ChatbotFaq> faqs = listFaqs();
        List<ChatbotIntent> intents = listIntents();
        return ChatbotAdminSummaryDTO.builder()
            .faqCount(faqs.size())
            .intentCount(intents.size())
            .enabledFaqCount(faqs.stream().filter(faq -> faq.enabled() == null || faq.enabled()).count())
            .enabledIntentCount(intents.stream().filter(intent -> intent.enabled() == null || intent.enabled()).count())
            .build();
    }
}
