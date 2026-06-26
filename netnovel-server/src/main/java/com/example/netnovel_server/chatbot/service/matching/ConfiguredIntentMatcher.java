package com.example.netnovel_server.chatbot.service.matching;

import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.service.ChatbotKnowledgeBase;
import com.example.netnovel_server.chatbot.service.language.ChatbotTextNormalizer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Matches configured/default intent examples from the knowledge base.
 *
 * This is separate from novel filter extraction because configured intents can
 * represent navigation or predefined filter presets from admin data.
 */
@Component
public class ConfiguredIntentMatcher {

    private final ChatbotKnowledgeBase knowledgeBase;
    private final ChatbotTextNormalizer normalizer;
    private final PhraseScorer phraseScorer;

    public ConfiguredIntentMatcher(
        ChatbotKnowledgeBase knowledgeBase,
        ChatbotTextNormalizer normalizer,
        PhraseScorer phraseScorer
    ) {
        this.knowledgeBase = knowledgeBase;
        this.normalizer = normalizer;
        this.phraseScorer = phraseScorer;
    }

    public ChatbotIntent match(String normalized, ChatbotLanguage language) {
        ChatbotIntent bestIntent = null;
        double bestScore = 0.0;
        String lang = language.code();

        for (ChatbotIntent intent : knowledgeBase.intents()) {
            for (String example : intent.examples().getOrDefault(lang, List.of())) {
                double score = phraseScorer.score(normalized, normalizer.normalize(example), language);
                if (score > bestScore) {
                    bestScore = score;
                    bestIntent = intent;
                }
            }
        }

        return bestScore >= 0.65 ? bestIntent : null;
    }
}
