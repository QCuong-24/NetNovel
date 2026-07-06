package com.example.netnovel_server.chatbot.service.knowledge;

import com.example.netnovel_server.chatbot.entity.ChatbotFaqDefinition;
import com.example.netnovel_server.chatbot.entity.ChatbotIntentDefinition;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotIntentAction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatbotKnowledgeMapperTest {

    // Knowledge mapper coverage: model <-> entity conversion and JSON text fields.

    private final ChatbotKnowledgeMapper mapper = new ChatbotKnowledgeMapper();

    @Test
    void mapsFaqModelToEntityAndBack() {
        ChatbotFaq faq = new ChatbotFaq(
            "bookmark_explanation",
            "feature",
            true,
            7,
            Map.of(
                "vi", List.of("bookmark là gì"),
                "en", List.of("what is bookmark")
            ),
            Map.of(
                "vi", "Bookmark giúp bạn lưu chương để đọc lại.",
                "en", "Bookmarks help you save chapters to read later."
            ),
            List.of("/collection"),
            List.of("collection", "bookmark")
        );

        ChatbotFaqDefinition entity = mapper.toEntity(faq);
        ChatbotFaq mapped = mapper.toModel(entity);

        assertEquals(faq.id(), entity.getId());
        assertEquals(faq.type(), entity.getType());
        assertEquals(faq.priority(), entity.getPriority());
        assertTrue(entity.getEnabled());
        assertEquals(faq, mapped);
    }

    @Test
    void mapsIntentModelToEntityAndBack() {
        ChatbotIntentAction action = new ChatbotIntentAction(
            Map.of("vi", "Mở bộ sưu tập", "en", "Open collection"),
            "navigate",
            "/collection",
            List.of("USER")
        );
        ChatbotIntent intent = new ChatbotIntent(
            "navigate_collection",
            "navigation",
            true,
            10,
            Map.of(
                "vi", List.of("mở bộ sưu tập"),
                "en", List.of("open my collection")
            ),
            Map.of(
                "vi", "Đây là bộ sưu tập của bạn.",
                "en", "Here is your collection."
            ),
            Map.of("scope", "collection"),
            List.of("navigation"),
            List.of(action)
        );

        ChatbotIntentDefinition entity = mapper.toEntity(intent);
        ChatbotIntent mapped = mapper.toModel(entity);

        assertEquals(intent.id(), entity.getId());
        assertEquals(intent.type(), entity.getType());
        assertEquals(intent.priority(), entity.getPriority());
        assertTrue(entity.getEnabled());
        assertEquals(intent, mapped);
    }

    @Test
    void mapsNullCollectionsToEmptyJsonCollections() {
        ChatbotFaq faq = new ChatbotFaq("empty_faq", null, null, null, null, null, null, null);
        ChatbotIntent intent = new ChatbotIntent("empty_intent", null, null, null, null, null, null, null, null);

        ChatbotFaq mappedFaq = mapper.toModel(mapper.toEntity(faq));
        ChatbotIntent mappedIntent = mapper.toModel(mapper.toEntity(intent));

        assertTrue(mappedFaq.enabled());
        assertEquals(0, mappedFaq.priority());
        assertTrue(mappedFaq.examples().isEmpty());
        assertTrue(mappedFaq.answers().isEmpty());
        assertTrue(mappedFaq.actionUrls().isEmpty());
        assertTrue(mappedFaq.tags().isEmpty());

        assertTrue(mappedIntent.enabled());
        assertEquals(0, mappedIntent.priority());
        assertTrue(mappedIntent.examples().isEmpty());
        assertTrue(mappedIntent.replies().isEmpty());
        assertTrue(mappedIntent.filters().isEmpty());
        assertTrue(mappedIntent.tags().isEmpty());
        assertTrue(mappedIntent.actions().isEmpty());
    }

    @Test
    void preservesDisabledDefinitions() {
        ChatbotFaq faq = new ChatbotFaq("disabled_faq", "faq", false, 0, Map.of(), Map.of(), List.of(), List.of());
        ChatbotIntent intent = new ChatbotIntent("disabled_intent", "search", false, 0, Map.of(), Map.of(), Map.of(), List.of(), List.of());

        assertFalse(mapper.toModel(mapper.toEntity(faq)).enabled());
        assertFalse(mapper.toModel(mapper.toEntity(intent)).enabled());
    }
}
