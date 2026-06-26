package com.example.netnovel_server.chatbot.service.knowledge;

import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotSynonyms;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Loads default chatbot knowledge from classpath resources:
 * - chatbot/faq.json
 * - chatbot/intents.json
 * - chatbot/synonyms.json
 * - chatbot/tags.txt
 *
 * tags.txt is optional and is merged into synonyms through ChatbotSynonymMerger.
 */
@Component
public class ChatbotKnowledgeResourceLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatbotSynonymMerger synonymMerger;

    public ChatbotKnowledgeResourceLoader() {
        this(new ChatbotSynonymMerger());
    }

    public ChatbotKnowledgeResourceLoader(ChatbotSynonymMerger synonymMerger) {
        this.synonymMerger = synonymMerger;
    }

    public List<ChatbotFaq> loadDefaultFaqs() {
        return read("chatbot/faq.json", new TypeReference<>() {});
    }

    public List<ChatbotIntent> loadDefaultIntents() {
        return read("chatbot/intents.json", new TypeReference<>() {});
    }

    public ChatbotSynonyms loadSynonyms() {
        ChatbotSynonyms baseSynonyms = read("chatbot/synonyms.json", new TypeReference<>() {});
        return synonymMerger.mergeTagSynonyms(baseSynonyms, readOptionalLines("chatbot/tags.txt"));
    }

    private <T> T read(String path, TypeReference<T> typeReference) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing chatbot resource: " + path);
            }
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load chatbot resource: " + path, exception);
        }
    }

    private List<String> readOptionalLines(String path) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                return List.of();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().toList();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load chatbot resource: " + path, exception);
        }
    }
}
