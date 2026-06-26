package com.example.netnovel_server.chatbot.service.response;

import com.example.netnovel_server.chatbot.dto.ChatbotActionDTO;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotIntentAction;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
public class ChatbotActionFactory {

    public List<ChatbotActionDTO> faqActions(List<String> urls, String language) {
        if (urls == null || urls.isEmpty()) {
            return List.of();
        }

        return urls.stream()
            .map(url -> navigateAction(actionLabel(url, language), url))
            .toList();
    }

    public List<ChatbotActionDTO> navigationActions(ChatbotIntent intent, String language) {
        if (intent.actions() == null) {
            return List.of();
        }

        return intent.actions().stream()
            .filter(this::canUseAction)
            .map(action -> ChatbotActionDTO.builder()
                .label(action.labels().getOrDefault(language, action.labels().getOrDefault("en", action.value())))
                .type(action.type())
                .value(action.value())
                .build())
            .toList();
    }

    public ChatbotActionDTO searchResultsAction(Map<String, String> filters, String language) {
        return navigateAction(text(language, "Xem thêm kết quả", "See more results"), searchUrl(filters));
    }

    public ChatbotActionDTO collectionAction(String language) {
        return navigateAction(text(language, "Mở bộ sưu tập", "Open collection"), "/collection");
    }

    public ChatbotActionDTO searchAction(String language) {
        return navigateAction(text(language, "Mở tìm kiếm", "Open search"), "/search");
    }

    private ChatbotActionDTO navigateAction(String label, String value) {
        return ChatbotActionDTO.builder()
            .label(label)
            .type("navigate")
            .value(value)
            .build();
    }

    private boolean canUseAction(ChatbotIntentAction action) {
        return action.requiredRoles() == null
            || action.requiredRoles().isEmpty()
            || SecurityUtils.hasAnyRole(action.requiredRoles().toArray(String[]::new));
    }

    private String actionLabel(String url, String language) {
        if ("/collection".equals(url)) {
            return text(language, "Mở bộ sưu tập", "Open collection");
        }
        if ("/search".equals(url)) {
            return text(language, "Mở tìm kiếm", "Open search");
        }
        return text(language, "Mở trang liên quan", "Open related page");
    }

    private String searchUrl(Map<String, String> filters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/search");
        addQueryParam(builder, "q", filters.get("q"));
        addQueryParam(builder, "status", filters.get("status"));
        addQueryParam(builder, "genre", filters.get("genre"));
        addQueryParam(builder, "sort", filters.get("sort"));
        if (!filters.containsKey("q")) {
            String fallbackQuery = filters.get("tag") != null ? filters.get("tag") : filters.get("author");
            addQueryParam(builder, "q", fallbackQuery);
        }

        return builder.build().encode().toUriString();
    }

    private void addQueryParam(UriComponentsBuilder builder, String key, String value) {
        if (value != null && !value.isBlank()) {
            builder.queryParam(key, value);
        }
    }

    private String text(String language, String vi, String en) {
        return "en".equals(language) ? en : vi;
    }
}
