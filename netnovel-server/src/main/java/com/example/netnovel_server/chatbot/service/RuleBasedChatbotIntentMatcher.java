package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.model.ChatbotSynonyms;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RuleBasedChatbotIntentMatcher implements ChatbotIntentMatcher {

    private final ChatbotKnowledgeBase knowledgeBase;
    private final ChatbotTextNormalizer normalizer;

    public RuleBasedChatbotIntentMatcher(ChatbotKnowledgeBase knowledgeBase, ChatbotTextNormalizer normalizer) {
        this.knowledgeBase = knowledgeBase;
        this.normalizer = normalizer;
    }

    @Override
    public ChatbotMatchResult match(String message, ChatbotLanguage language) {
        String normalized = normalizer.normalize(message);
        if (normalized.isBlank()) {
            return ChatbotMatchResult.fallback(language);
        }

        ChatbotMatchResult faqMatch = matchFaq(normalized, language);
        ChatbotMatchResult intentMatch = matchSearchIntent(normalized, language);

        if (faqMatch.confidence() >= intentMatch.confidence()) {
            return faqMatch;
        }

        return intentMatch;
    }

    private ChatbotMatchResult matchFaq(String normalized, ChatbotLanguage language) {
        ChatbotFaq bestFaq = null;
        double bestScore = 0.0;
        String lang = language.code();

        for (ChatbotFaq faq : knowledgeBase.faqs()) {
            for (String example : faq.examples().getOrDefault(lang, List.of())) {
                String normalizedExample = normalizer.normalize(example);
                double score = phraseScore(normalized, normalizedExample);
                if (score > bestScore) {
                    bestScore = score;
                    bestFaq = faq;
                }
            }
        }

        if (bestFaq == null || bestScore < 0.45) {
            return ChatbotMatchResult.fallback(language);
        }

        return new ChatbotMatchResult("faq", language, Math.min(bestScore, 0.98), Map.of(), bestFaq);
    }

    private ChatbotMatchResult matchSearchIntent(String normalized, ChatbotLanguage language) {
        Map<String, String> filters = new HashMap<>();
        double score = 0.0;
        String intent = "search_novel";
        ChatbotSynonyms synonyms = knowledgeBase.synonyms();

        ChatbotIntent configuredIntent = matchConfiguredIntent(normalized, language);
        if (configuredIntent != null) {
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

        String query = extractQuery(normalized, filters);
        if (!query.isBlank()) {
            filters.put("q", query);
            score += 0.25;
        }

        if (containsAny(normalized, "truyen", "novel", "novels") || containsAny(normalized, synonyms.searchWords().getOrDefault("default", List.of()))) {
            score += 0.2;
        }

        if (filters.isEmpty() && score < 0.45) {
            return ChatbotMatchResult.fallback(language);
        }

        return new ChatbotMatchResult(intent, language, Math.min(score, 0.95), filters, null);
    }

    private ChatbotIntent matchConfiguredIntent(String normalized, ChatbotLanguage language) {
        ChatbotIntent bestIntent = null;
        double bestScore = 0.0;
        String lang = language.code();

        for (ChatbotIntent intent : knowledgeBase.intents()) {
            for (String example : intent.examples().getOrDefault(lang, List.of())) {
                double score = phraseScore(normalized, normalizer.normalize(example));
                if (score > bestScore) {
                    bestScore = score;
                    bestIntent = intent;
                }
            }
        }

        return bestScore >= 0.65 ? bestIntent : null;
    }

    private double phraseScore(String message, String example) {
        if (message.equals(example)) {
            return 1.0;
        }
        if (message.contains(example) || example.contains(message)) {
            return 0.85;
        }

        String[] words = example.split(" ");
        int matched = 0;
        for (String word : words) {
            if (word.length() > 2 && message.contains(word)) {
                matched++;
            }
        }

        return words.length == 0 ? 0.0 : (double) matched / words.length;
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
                    .replaceAll("\\b(truyen|novel|novels|cua|of)\\b", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
                if (extracted.length() >= 2) {
                    return extracted;
                }
            }
        }

        return "";
    }

    private String extractQuery(String normalized, Map<String, String> filters) {
        if (!containsAny(normalized, knowledgeBase.synonyms().searchWords().getOrDefault("default", List.of()))) {
            return "";
        }

        String cleaned = normalized
            .replaceAll("\\b(tim kiem|tim|show me|find|search|recommend|goi y|truyen|novels|novel|cho toi|cho minh|please|for|me)\\b", " ");

        for (String value : filters.values()) {
            cleaned = cleaned.replace(normalizer.normalize(value), " ");
        }

        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned.length() >= 3 ? cleaned : "";
    }
}
