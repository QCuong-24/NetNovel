package com.example.netnovel_server.chatbot.service.matching;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.service.ChatbotKnowledgeBase;
import com.example.netnovel_server.chatbot.service.language.ChatbotTextNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Rule-based chatbot intent orchestrator.
 *
 * Keep this class focused on flow only:
 * normalize input -> clarification -> FAQ match -> novel/configured intent match -> choose best result.
 *
 * Detailed scoring, FAQ matching, configured intent matching, and novel filter extraction
 * live in dedicated collaborators in this package.
 */
@Component
public class RuleBasedChatbotIntentMatcher implements ChatbotIntentMatcher {

    private final ChatbotTextNormalizer normalizer;
    private final ChatbotClarificationMatcher clarificationMatcher;
    private final ChatbotFaqMatcher faqMatcher;
    private final NovelFilterExtractor novelFilterExtractor;

    public RuleBasedChatbotIntentMatcher(ChatbotKnowledgeBase knowledgeBase, ChatbotTextNormalizer normalizer) {
        this(
            normalizer,
            new ChatbotClarificationMatcher(),
            new ChatbotFaqMatcher(knowledgeBase, normalizer, new PhraseScorer(knowledgeBase)),
            new NovelFilterExtractor(
                knowledgeBase,
                normalizer,
                new ConfiguredIntentMatcher(knowledgeBase, normalizer, new PhraseScorer(knowledgeBase))
            )
        );
    }

    @Autowired
    public RuleBasedChatbotIntentMatcher(
        ChatbotTextNormalizer normalizer,
        ChatbotClarificationMatcher clarificationMatcher,
        ChatbotFaqMatcher faqMatcher,
        NovelFilterExtractor novelFilterExtractor
    ) {
        this.normalizer = normalizer;
        this.clarificationMatcher = clarificationMatcher;
        this.faqMatcher = faqMatcher;
        this.novelFilterExtractor = novelFilterExtractor;
    }

    @Override
    public ChatbotMatchResult match(String message, ChatbotLanguage language) {
        String normalized = normalizer.normalize(message);
        if (normalized.isBlank()) {
            return ChatbotMatchResult.fallback(language);
        }

        ChatbotMatchResult clarification = clarificationMatcher.match(normalized, language);
        if (clarification != null) {
            return clarification;
        }

        ChatbotMatchResult faqMatch = faqMatcher.match(normalized, language);
        ChatbotMatchResult intentMatch = novelFilterExtractor.match(normalized, language);

        return chooseBestMatch(faqMatch, intentMatch, language);
    }

    private ChatbotMatchResult chooseBestMatch(
        ChatbotMatchResult faqMatch,
        ChatbotMatchResult intentMatch,
        ChatbotLanguage language
    ) {
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
}
