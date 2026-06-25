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

        ChatbotMatchResult clarification = matchClarification(normalized, language);
        if (clarification != null) {
            return clarification;
        }

        ChatbotMatchResult faqMatch = matchFaq(normalized, language);
        ChatbotMatchResult intentMatch = matchIntent(normalized, language);

        if ("title".equals(intentMatch.filters().get("scope"))) {
            return intentMatch;
        }

        if (
            faqMatch.confidence() >= 0.45
                && intentMatch.confidence() >= 0.45
                && intentMatch.filters().isEmpty()
                && Math.abs(faqMatch.confidence() - intentMatch.confidence()) < 0.15
        ) {
            return ChatbotMatchResult.clarify(language, "general", Math.max(faqMatch.confidence(), intentMatch.confidence()));
        }

        if (faqMatch.confidence() >= intentMatch.confidence()) {
            return faqMatch;
        }

        return intentMatch;
    }

    private ChatbotMatchResult matchClarification(String normalized, ChatbotLanguage language) {
        if (containsAny(normalized, "luu truyen", "luu lai", "save novel", "save story")) {
            return ChatbotMatchResult.clarify(language, "save_novel", 0.4);
        }

        if (containsAny(normalized, "truyen hay", "truyen nao hay", "good novel", "good novels", "recommend something")) {
            return ChatbotMatchResult.clarify(language, "search", 0.35);
        }

        return null;
    }

    private ChatbotMatchResult matchFaq(String normalized, ChatbotLanguage language) {
        ChatbotFaq bestFaq = null;
        double bestScore = 0.0;
        String lang = language.code();

        for (ChatbotFaq faq : knowledgeBase.faqs()) {
            for (String example : faq.examples().getOrDefault(lang, List.of())) {
                String normalizedExample = normalizer.normalize(example);
                double score = phraseScore(normalized, normalizedExample, language);
                if (score > bestScore) {
                    bestScore = score;
                    bestFaq = faq;
                }
            }
        }

        if (bestFaq == null || bestScore < 0.45) {
            return ChatbotMatchResult.fallback(language);
        }

        return new ChatbotMatchResult("faq", language, Math.min(bestScore, 0.98), 0.0, false, null, Map.of(), bestFaq, null);
    }

    private ChatbotMatchResult matchIntent(String normalized, ChatbotLanguage language) {
        Map<String, String> filters = new HashMap<>();
        double score = 0.0;
        String intent = "search_novel";
        ChatbotSynonyms synonyms = knowledgeBase.synonyms();

        ChatbotIntent configuredIntent = matchConfiguredIntent(normalized, language);
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

        String query = extractQuery(normalized, filters);
        if (!query.isBlank() && !filters.containsKey("q")) {
            filters.put("q", query);
            score += 0.25;
        }

        if (containsAny(normalized, "truyen", "novel", "novels") || containsAny(normalized, synonyms.searchWords().getOrDefault("default", List.of()))) {
            score += 0.2;
        }

        if (filters.isEmpty() && score < 0.45) {
            return ChatbotMatchResult.fallback(language);
        }

        return new ChatbotMatchResult(intent, language, Math.min(score, 0.95), 0.0, false, null, filters, null, configuredIntent);
    }

    private ChatbotIntent matchConfiguredIntent(String normalized, ChatbotLanguage language) {
        ChatbotIntent bestIntent = null;
        double bestScore = 0.0;
        String lang = language.code();

        for (ChatbotIntent intent : knowledgeBase.intents()) {
            for (String example : intent.examples().getOrDefault(lang, List.of())) {
                double score = phraseScore(normalized, normalizer.normalize(example), language);
                if (score > bestScore) {
                    bestScore = score;
                    bestIntent = intent;
                }
            }
        }

        return bestScore >= 0.65 ? bestIntent : null;
    }

    private double phraseScore(String message, String example) {
        return phraseScore(message, example, ChatbotLanguage.VI);
    }

    private double phraseScore(String message, String example, ChatbotLanguage language) {
        if (message.equals(example)) {
            return 1.0;
        }
        if (message.contains(example) || example.contains(message)) {
            return 0.85;
        }

        String[] words = example.split(" ");
        int matched = 0;
        int importantWords = 0;
        List<String> stopwords = knowledgeBase.synonyms().stopwords().getOrDefault(language.code(), List.of());
        for (String word : words) {
            if (word.length() > 2 && !stopwords.contains(word)) {
                importantWords++;
                if (message.contains(word)) {
                    matched++;
                }
            }
        }

        if (importantWords == 0) {
            return 0.0;
        }

        double score = (double) matched / importantWords;
        return importantWords < 3 ? Math.min(score, 0.55) : score;
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
