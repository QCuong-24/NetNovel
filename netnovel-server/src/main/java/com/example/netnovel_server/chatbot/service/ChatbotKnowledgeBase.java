package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.entity.ChatbotFaqDefinition;
import com.example.netnovel_server.chatbot.entity.ChatbotIntentDefinition;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotSynonyms;
import com.example.netnovel_server.chatbot.repository.ChatbotFaqDefinitionRepository;
import com.example.netnovel_server.chatbot.repository.ChatbotIntentDefinitionRepository;
import com.example.netnovel_server.chatbot.service.knowledge.ChatbotKnowledgeMapper;
import com.example.netnovel_server.chatbot.service.knowledge.ChatbotKnowledgeResourceLoader;
import com.example.netnovel_server.chatbot.service.knowledge.ChatbotKnowledgeSeeder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Facade/cache for chatbot knowledge used by matchers, responses, admin APIs, and embedding reindex.
 *
 * Keep the public API here stable:
 * faqs(), intents(), allFaqs(), allIntents(), reload(), synonyms(),
 * importDefaults(), findFaqById(), findIntentById().
 *
 * Heavy responsibilities are delegated to:
 * - ChatbotKnowledgeResourceLoader: reads default JSON resources and tags.txt.
 * - ChatbotKnowledgeMapper: converts DB entities <-> chatbot models.
 * - ChatbotKnowledgeSeeder: seeds/imports default knowledge into the database.
 * - ChatbotSynonymMerger: merges tags.txt aliases into synonym definitions.
 */
@Component
public class ChatbotKnowledgeBase {

    private final List<ChatbotFaq> defaultFaqs;
    private final List<ChatbotIntent> defaultIntents;
    private final ChatbotSynonyms synonyms;
    private final ChatbotFaqDefinitionRepository faqRepository;
    private final ChatbotIntentDefinitionRepository intentRepository;
    private final ChatbotKnowledgeMapper mapper;
    private final ChatbotKnowledgeSeeder seeder;
    private volatile List<ChatbotFaq> faqCache;
    private volatile List<ChatbotIntent> intentCache;

    public ChatbotKnowledgeBase() {
        ChatbotKnowledgeResourceLoader resourceLoader = new ChatbotKnowledgeResourceLoader();
        this.mapper = new ChatbotKnowledgeMapper();
        this.defaultFaqs = resourceLoader.loadDefaultFaqs();
        this.defaultIntents = resourceLoader.loadDefaultIntents();
        this.synonyms = resourceLoader.loadSynonyms();
        this.faqRepository = null;
        this.intentRepository = null;
        this.seeder = null;
        reload();
    }

    @Autowired
    public ChatbotKnowledgeBase(
        ChatbotFaqDefinitionRepository faqRepository,
        ChatbotIntentDefinitionRepository intentRepository,
        ChatbotKnowledgeResourceLoader resourceLoader,
        ChatbotKnowledgeMapper mapper,
        ChatbotKnowledgeSeeder seeder
    ) {
        this.defaultFaqs = resourceLoader.loadDefaultFaqs();
        this.defaultIntents = resourceLoader.loadDefaultIntents();
        this.synonyms = resourceLoader.loadSynonyms();
        this.faqRepository = faqRepository;
        this.intentRepository = intentRepository;
        this.mapper = mapper;
        this.seeder = seeder;
        seeder.seedDefaultsIfEmpty(defaultFaqs, defaultIntents);
        reload();
    }

    public List<ChatbotFaq> faqs() {
        return faqCache.stream()
            .filter(faq -> faq.enabled() == null || faq.enabled())
            .sorted(Comparator.comparing((ChatbotFaq faq) -> faq.priority() == null ? 0 : faq.priority()).reversed())
            .toList();
    }

    public List<ChatbotIntent> intents() {
        return intentCache.stream()
            .filter(intent -> intent.enabled() == null || intent.enabled())
            .sorted(Comparator.comparing((ChatbotIntent intent) -> intent.priority() == null ? 0 : intent.priority()).reversed())
            .toList();
    }

    public List<ChatbotFaq> allFaqs() {
        return faqCache.stream()
            .sorted(Comparator.comparing((ChatbotFaq faq) -> faq.priority() == null ? 0 : faq.priority()).reversed())
            .toList();
    }

    public List<ChatbotIntent> allIntents() {
        return intentCache.stream()
            .sorted(Comparator.comparing((ChatbotIntent intent) -> intent.priority() == null ? 0 : intent.priority()).reversed())
            .toList();
    }

    public void reload() {
        this.faqCache = loadFaqs();
        this.intentCache = loadIntents();
    }

    public ChatbotSynonyms synonyms() {
        return synonyms;
    }

    public Optional<ChatbotFaq> findFaqById(String id) {
        return faqs().stream()
            .filter(faq -> faq.id().equals(id))
            .findFirst();
    }

    public Optional<ChatbotIntent> findIntentById(String id) {
        return intents().stream()
            .filter(intent -> intent.id().equals(id))
            .findFirst();
    }

    public List<String> suggestions(String language) {
        if ("en".equals(language)) {
            return List.of("Popular novels", "Latest updates", "Completed novels", "How to follow a novel?");
        }

        return List.of("Truyện hot", "Truyện mới cập nhật", "Truyện hoàn thành", "Làm sao theo dõi truyện?");
    }

    public void importDefaults(boolean replaceExisting) {
        if (seeder == null) {
            return;
        }

        seeder.importDefaults(defaultFaqs, defaultIntents, replaceExisting);
        reload();
    }

    private List<ChatbotFaq> loadFaqs() {
        if (faqRepository == null || faqRepository.count() == 0) {
            return defaultFaqs;
        }
        return faqRepository.findAll().stream().map(mapper::toModel).toList();
    }

    private List<ChatbotIntent> loadIntents() {
        if (intentRepository == null || intentRepository.count() == 0) {
            return defaultIntents;
        }
        return intentRepository.findAll().stream().map(mapper::toModel).toList();
    }

    public ChatbotFaqDefinition toEntity(ChatbotFaq faq) {
        return mapper.toEntity(faq);
    }

    public ChatbotIntentDefinition toEntity(ChatbotIntent intent) {
        return mapper.toEntity(intent);
    }

    public ChatbotFaq toModel(ChatbotFaqDefinition entity) {
        return mapper.toModel(entity);
    }

    public ChatbotIntent toModel(ChatbotIntentDefinition entity) {
        return mapper.toModel(entity);
    }
}
