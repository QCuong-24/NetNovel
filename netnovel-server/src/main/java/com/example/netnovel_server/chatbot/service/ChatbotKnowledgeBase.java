package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotSynonyms;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

@Component
public class ChatbotKnowledgeBase {

    private final List<ChatbotFaq> faqs;
    private final List<ChatbotIntent> intents;
    private final ChatbotSynonyms synonyms;

    public ChatbotKnowledgeBase() {
        ObjectMapper objectMapper = new ObjectMapper();
        this.faqs = read(objectMapper, "chatbot/faq.json", new TypeReference<>() {});
        this.intents = read(objectMapper, "chatbot/intents.json", new TypeReference<>() {});
        this.synonyms = read(objectMapper, "chatbot/synonyms.json", new TypeReference<>() {});
    }

    public List<ChatbotFaq> faqs() {
        return faqs.stream()
            .filter(faq -> faq.enabled() == null || faq.enabled())
            .sorted(Comparator.comparing((ChatbotFaq faq) -> faq.priority() == null ? 0 : faq.priority()).reversed())
            .toList();
    }

    public List<ChatbotIntent> intents() {
        return intents.stream()
            .filter(intent -> intent.enabled() == null || intent.enabled())
            .sorted(Comparator.comparing((ChatbotIntent intent) -> intent.priority() == null ? 0 : intent.priority()).reversed())
            .toList();
    }

    public ChatbotSynonyms synonyms() {
        return synonyms;
    }

    public List<String> suggestions(String language) {
        if ("en".equals(language)) {
            return List.of("Popular novels", "Latest updates", "Completed novels", "How to follow a novel?");
        }

        return List.of("Truyện hot", "Truyện mới cập nhật", "Truyện hoàn thành", "Làm sao theo dõi truyện?");
    }

    private <T> T read(ObjectMapper objectMapper, String path, TypeReference<T> typeReference) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing chatbot resource: " + path);
            }
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load chatbot resource: " + path, exception);
        }
    }
}
