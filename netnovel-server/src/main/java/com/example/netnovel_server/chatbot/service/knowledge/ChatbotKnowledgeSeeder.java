package com.example.netnovel_server.chatbot.service.knowledge;

import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.repository.ChatbotFaqDefinitionRepository;
import com.example.netnovel_server.chatbot.repository.ChatbotIntentDefinitionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds and imports default chatbot FAQ/intent definitions into the database.
 *
 * Used by ChatbotKnowledgeBase during startup and by admin import-defaults.
 */
@Component
public class ChatbotKnowledgeSeeder {

    private final ChatbotFaqDefinitionRepository faqRepository;
    private final ChatbotIntentDefinitionRepository intentRepository;
    private final ChatbotKnowledgeMapper mapper;

    public ChatbotKnowledgeSeeder(
        ChatbotFaqDefinitionRepository faqRepository,
        ChatbotIntentDefinitionRepository intentRepository,
        ChatbotKnowledgeMapper mapper
    ) {
        this.faqRepository = faqRepository;
        this.intentRepository = intentRepository;
        this.mapper = mapper;
    }

    public void seedDefaultsIfEmpty(List<ChatbotFaq> defaultFaqs, List<ChatbotIntent> defaultIntents) {
        if (faqRepository.count() == 0) {
            faqRepository.saveAll(defaultFaqs.stream().map(mapper::toEntity).toList());
        }
        if (intentRepository.count() == 0) {
            intentRepository.saveAll(defaultIntents.stream().map(mapper::toEntity).toList());
        }
    }

    public void importDefaults(
        List<ChatbotFaq> defaultFaqs,
        List<ChatbotIntent> defaultIntents,
        boolean replaceExisting
    ) {
        if (replaceExisting) {
            faqRepository.deleteAll();
            intentRepository.deleteAll();
        }
        for (ChatbotFaq faq : defaultFaqs) {
            if (replaceExisting || !faqRepository.existsById(faq.id())) {
                faqRepository.save(mapper.toEntity(faq));
            }
        }
        for (ChatbotIntent intent : defaultIntents) {
            if (replaceExisting || !intentRepository.existsById(intent.id())) {
                intentRepository.save(mapper.toEntity(intent));
            }
        }
    }
}
