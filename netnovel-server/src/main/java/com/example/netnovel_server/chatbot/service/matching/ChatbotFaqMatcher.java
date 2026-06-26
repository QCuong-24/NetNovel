package com.example.netnovel_server.chatbot.service.matching;

import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.service.ChatbotKnowledgeBase;
import com.example.netnovel_server.chatbot.service.language.ChatbotTextNormalizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Matches normalized user messages against FAQ examples.
 *
 * This only decides whether a FAQ is the best match. Building the final reply
 * still belongs to the response factory.
 */
@Component
public class ChatbotFaqMatcher {

    private final ChatbotKnowledgeBase knowledgeBase;
    private final ChatbotTextNormalizer normalizer;
    private final PhraseScorer phraseScorer;

    public ChatbotFaqMatcher(
        ChatbotKnowledgeBase knowledgeBase,
        ChatbotTextNormalizer normalizer,
        PhraseScorer phraseScorer
    ) {
        this.knowledgeBase = knowledgeBase;
        this.normalizer = normalizer;
        this.phraseScorer = phraseScorer;
    }

    public ChatbotMatchResult match(String normalized, ChatbotLanguage language) {
        ChatbotFaq bestFaq = null;
        double bestScore = 0.0;
        String lang = language.code();

        for (ChatbotFaq faq : knowledgeBase.faqs()) {
            for (String example : faq.examples().getOrDefault(lang, List.of())) {
                String normalizedExample = normalizer.normalize(example);
                double score = phraseScorer.score(normalized, normalizedExample, language);
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
}
