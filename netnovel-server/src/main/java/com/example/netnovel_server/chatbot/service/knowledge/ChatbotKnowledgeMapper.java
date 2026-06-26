package com.example.netnovel_server.chatbot.service.knowledge;

import com.example.netnovel_server.chatbot.entity.ChatbotFaqDefinition;
import com.example.netnovel_server.chatbot.entity.ChatbotIntentDefinition;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Converts chatbot knowledge between API/domain models and database entities.
 *
 * Owns JSON serialization/deserialization for fields stored as text:
 * examples, answers, replies, filters, tags, actions, and action URLs.
 */
@Component
public class ChatbotKnowledgeMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatbotFaqDefinition toEntity(ChatbotFaq faq) {
        return ChatbotFaqDefinition.builder()
            .id(faq.id())
            .type(faq.type())
            .enabled(faq.enabled() == null || faq.enabled())
            .priority(faq.priority() == null ? 0 : faq.priority())
            .examplesJson(json(faq.examples() == null ? Map.of() : faq.examples()))
            .answersJson(json(faq.answers() == null ? Map.of() : faq.answers()))
            .actionUrlsJson(json(faq.actionUrls() == null ? List.of() : faq.actionUrls()))
            .tagsJson(json(faq.tags() == null ? List.of() : faq.tags()))
            .build();
    }

    public ChatbotIntentDefinition toEntity(ChatbotIntent intent) {
        return ChatbotIntentDefinition.builder()
            .id(intent.id())
            .type(intent.type())
            .enabled(intent.enabled() == null || intent.enabled())
            .priority(intent.priority() == null ? 0 : intent.priority())
            .examplesJson(json(intent.examples() == null ? Map.of() : intent.examples()))
            .repliesJson(json(intent.replies() == null ? Map.of() : intent.replies()))
            .filtersJson(json(intent.filters() == null ? Map.of() : intent.filters()))
            .tagsJson(json(intent.tags() == null ? List.of() : intent.tags()))
            .actionsJson(json(intent.actions() == null ? List.of() : intent.actions()))
            .build();
    }

    public ChatbotFaq toModel(ChatbotFaqDefinition entity) {
        return new ChatbotFaq(
            entity.getId(),
            entity.getType(),
            entity.getEnabled(),
            entity.getPriority(),
            readJson(entity.getExamplesJson(), new TypeReference<>() {}),
            readJson(entity.getAnswersJson(), new TypeReference<>() {}),
            readJson(entity.getActionUrlsJson(), new TypeReference<>() {}),
            readJson(entity.getTagsJson(), new TypeReference<>() {})
        );
    }

    public ChatbotIntent toModel(ChatbotIntentDefinition entity) {
        return new ChatbotIntent(
            entity.getId(),
            entity.getType(),
            entity.getEnabled(),
            entity.getPriority(),
            readJson(entity.getExamplesJson(), new TypeReference<>() {}),
            readJson(entity.getRepliesJson(), new TypeReference<>() {}),
            readJson(entity.getFiltersJson(), new TypeReference<>() {}),
            readJson(entity.getTagsJson(), new TypeReference<>() {}),
            readJson(entity.getActionsJson(), new TypeReference<>() {})
        );
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize chatbot knowledge value", exception);
        }
    }

    private <T> T readJson(String value, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not deserialize chatbot knowledge value", exception);
        }
    }
}
