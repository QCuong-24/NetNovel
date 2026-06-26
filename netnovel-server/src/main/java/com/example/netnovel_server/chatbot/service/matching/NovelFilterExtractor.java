package com.example.netnovel_server.chatbot.service.matching;

import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.model.ChatbotSynonyms;
import com.example.netnovel_server.chatbot.service.ChatbotKnowledgeBase;
import com.example.netnovel_server.chatbot.service.language.ChatbotTextNormalizer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts novel-search filters from a normalized message.
 *
 * Owns genre/tag/status/sort/author/title/free-query detection and decides
 * whether the resulting intent should be filtered_novels, search_by_title,
 * popular_novels, latest_novels, or a configured intent.
 */
@Component
public class NovelFilterExtractor {

    private final ChatbotKnowledgeBase knowledgeBase;
    private final ChatbotTextNormalizer normalizer;
    private final ConfiguredIntentMatcher configuredIntentMatcher;

    public NovelFilterExtractor(
        ChatbotKnowledgeBase knowledgeBase,
        ChatbotTextNormalizer normalizer,
        ConfiguredIntentMatcher configuredIntentMatcher
    ) {
        this.knowledgeBase = knowledgeBase;
        this.normalizer = normalizer;
        this.configuredIntentMatcher = configuredIntentMatcher;
    }

    public ChatbotMatchResult match(String normalized, ChatbotLanguage language) {
        Map<String, String> filters = new HashMap<>();
        double score = 0.0;
        String intent = "search_novel";
        ChatbotSynonyms synonyms = knowledgeBase.synonyms();

        ChatbotIntent configuredIntent = configuredIntentMatcher.match(normalized, language);
        if (configuredIntent != null) {
            if ("navigation".equals(configuredIntent.type())) {
                return new ChatbotMatchResult(
                    configuredIntent.id(),
                    language,
                    0.9,
                    0.0,
                    false,
                    null,
                    Map.of(),
                    null,
                    configuredIntent
                );
            }
            filters.putAll(configuredIntent.filters());
            intent = configuredIntent.id();
            score += 0.55;
        }

        String sort = detectFromSynonyms(normalized, synonyms.sorts());
        if (!sort.isBlank()) {
            filters.put("sort", sort);
            intent = "popular".equals(sort) ? "popular_novels" : "latest_novels";
            score += 0.35;
        }

        String status = detectFromSynonyms(normalized, synonyms.statuses());
        if (!status.isBlank()) {
            filters.put("status", status);
            score += 0.35;
        }

        String genre = detectFromSynonyms(normalized, synonyms.genres());
        if (!genre.isBlank()) {
            filters.put("genre", genre);
            score += 0.35;
        }

        String tag = detectFromSynonyms(normalized, synonyms.tags());
        if (!tag.isBlank()) {
            filters.put("tag", tag);
            score += 0.3;
        }

        String author = extractAfterAny(normalized, synonyms.authorWords().getOrDefault("default", List.of()));
        if (!author.isBlank()) {
            filters.put("author", author);
            score += 0.4;
        }

        String titleQuery = extractAfterAny(normalized, synonyms.titleWords().getOrDefault("default", List.of()));
        if (!titleQuery.isBlank()) {
            filters.put("q", titleQuery);
            filters.put("scope", "title");
            intent = "search_by_title";
            score += 0.45;
        }

        String query = extractQuery(normalized, filters, synonyms);
        if (!query.isBlank() && !filters.containsKey("q")) {
            filters.put("q", query);
            score += 0.25;
        }

        if (containsAny(normalized, "truyen", "novel", "novels") || containsAny(normalized, synonyms.searchWords().getOrDefault("default", List.of()))) {
            score += 0.2;
        }

        if (shouldUseFilteredNovelIntent(intent, filters)) {
            intent = "filtered_novels";
        }

        if (filters.isEmpty() && score < 0.45) {
            return ChatbotMatchResult.fallback(language);
        }

        return new ChatbotMatchResult(intent, language, Math.min(score, 0.95), 0.0, false, null, filters, null, configuredIntent);
    }

    private boolean shouldUseFilteredNovelIntent(String intent, Map<String, String> filters) {
        if (filters.isEmpty()) {
            return false;
        }
        if ("popular_novels".equals(intent) || "latest_novels".equals(intent) || "search_by_title".equals(intent)) {
            return false;
        }
        return filters.containsKey("genre")
            || filters.containsKey("tag")
            || filters.containsKey("status")
            || filters.containsKey("author")
            || filters.containsKey("q");
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAny(String value, List<String> candidates) {
        for (String candidate : candidates) {
            if (value.contains(normalizer.normalize(candidate))) {
                return true;
            }
        }
        return false;
    }

    private String detectFromSynonyms(String normalized, Map<String, List<String>> synonyms) {
        if (synonyms == null) {
            return "";
        }

        for (Map.Entry<String, List<String>> entry : synonyms.entrySet()) {
            for (String alias : entry.getValue()) {
                if (normalized.contains(normalizer.normalize(alias))) {
                    return entry.getKey();
                }
            }
        }

        return "";
    }

    private String extractAfterAny(String normalized, List<String> markers) {
        for (String marker : markers) {
            String normalizedMarker = normalizer.normalize(marker);
            int index = normalized.indexOf(normalizedMarker);
            if (index >= 0) {
                String extracted = normalized.substring(index + normalizedMarker.length())
                    .replaceAll("\\b(truyen|novel|novels|story|stories|cua|of|with|has|have)\\b", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
                if (extracted.length() >= 2) {
                    return extracted;
                }
            }
        }

        return "";
    }

    private String extractQuery(String normalized, Map<String, String> filters, ChatbotSynonyms synonyms) {
        if (!containsAny(normalized, knowledgeBase.synonyms().searchWords().getOrDefault("default", List.of()))) {
            return "";
        }

        String cleaned = normalized
            .replaceAll("\\b(tim kiem|tim|show me|find|search|recommend|goi y|truyen|novels|novel|cho toi|cho minh|please|for|me)\\b", " ");

        for (String value : filters.values()) {
            cleaned = cleaned.replace(normalizer.normalize(value), " ");
        }

        cleaned = removeAliases(cleaned, filters.get("genre"), synonyms.genres());
        cleaned = removeAliases(cleaned, filters.get("tag"), synonyms.tags());
        cleaned = removeAliases(cleaned, filters.get("status"), synonyms.statuses());

        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned.length() >= 3 ? cleaned : "";
    }

    private String removeAliases(String value, String canonicalValue, Map<String, List<String>> synonyms) {
        if (canonicalValue == null || canonicalValue.isBlank() || synonyms == null) {
            return value;
        }

        String cleaned = value;
        for (String alias : synonyms.getOrDefault(canonicalValue, List.of())) {
            cleaned = cleaned.replace(normalizer.normalize(alias), " ");
        }
        return cleaned;
    }
}
