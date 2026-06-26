package com.example.netnovel_server.chatbot.service.matching;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.service.ChatbotKnowledgeBase;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scores how closely a normalized user message matches a normalized FAQ/intent example.
 *
 * Keep phrase scoring rules here so FAQ matching and configured intent matching
 * use the same behavior.
 */
@Component
public class PhraseScorer {

    private final ChatbotKnowledgeBase knowledgeBase;

    public PhraseScorer(ChatbotKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public double score(String message, String example, ChatbotLanguage language) {
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
}
