package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.dto.ChatbotActionDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotRequestDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotResponseDTO;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.dto.NovelDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    private final ChatbotLanguageDetector languageDetector;
    private final ChatbotIntentMatcher intentMatcher;
    private final ChatbotKnowledgeBase knowledgeBase;
    private final ChatbotNovelSearchService novelSearchService;
    private final ChatbotFallbackLogger fallbackLogger;

    public ChatbotService(
        ChatbotLanguageDetector languageDetector,
        ChatbotIntentMatcher intentMatcher,
        ChatbotKnowledgeBase knowledgeBase,
        ChatbotNovelSearchService novelSearchService,
        ChatbotFallbackLogger fallbackLogger
    ) {
        this.languageDetector = languageDetector;
        this.intentMatcher = intentMatcher;
        this.knowledgeBase = knowledgeBase;
        this.novelSearchService = novelSearchService;
        this.fallbackLogger = fallbackLogger;
    }

    public ChatbotResponseDTO handle(ChatbotRequestDTO request) {
        String message = request != null ? request.getMessage() : "";
        ChatbotLanguage language = languageDetector.detect(message, request != null ? request.getLanguage() : null);
        ChatbotMatchResult match = intentMatcher.match(message, language);

        if ("faq".equals(match.intent()) && match.faq() != null) {
            return faqResponse(match);
        }

        if (match.confidence() >= 0.45 && match.intent().contains("novel")) {
            ChatbotResponseDTO response = novelSearchResponse(match);
            if (response.getNovels().isEmpty()) {
                fallbackLogger.log(message, language, match.intent(), match.confidence(), match.filters(), 0);
            }
            return response;
        }

        fallbackLogger.log(message, language, match.intent(), match.confidence(), match.filters(), null);
        return fallbackResponse(language);
    }

    private ChatbotResponseDTO faqResponse(ChatbotMatchResult match) {
        ChatbotFaq faq = match.faq();
        String language = match.language().code();
        return ChatbotResponseDTO.builder()
            .reply(faq.answers().getOrDefault(language, faq.answers().get("vi")))
            .language(language)
            .intent(faq.id())
            .confidence(match.confidence())
            .novels(List.of())
            .suggestedQuestions(knowledgeBase.suggestions(language))
            .actions(faq.actionUrls().stream()
                .map(url -> ChatbotActionDTO.builder()
                    .label(actionLabel(url, language))
                    .type("navigate")
                    .value(url)
                    .build())
                .toList())
            .build();
    }

    private ChatbotResponseDTO novelSearchResponse(ChatbotMatchResult match) {
        String language = match.language().code();
        List<NovelDTO> novels = novelSearchService.search(match.filters());

        String reply = novels.isEmpty()
            ? text(language, "Mình chưa tìm thấy truyện phù hợp. Bạn thử đổi thể loại hoặc từ khóa nhé.",
                "I couldn't find matching novels yet. Try another genre or keyword.")
            : switch (match.intent()) {
                case "popular_novels" -> text(language, "Đây là một số truyện nổi bật cho bạn.", "Here are some popular novels for you.");
                case "latest_novels" -> text(language, "Đây là một số truyện mới cập nhật.", "Here are some recently updated novels.");
                default -> text(language, "Mình tìm thấy một số truyện phù hợp cho bạn.", "I found some novels that may fit your request.");
            };

        return ChatbotResponseDTO.builder()
            .reply(reply)
            .language(language)
            .intent(match.intent())
            .confidence(match.confidence())
            .novels(novels)
            .suggestedQuestions(knowledgeBase.suggestions(language))
            .actions(List.of(ChatbotActionDTO.builder()
                .label(text(language, "Xem thêm kết quả", "See more results"))
                .type("navigate")
                .value(searchUrl(match.filters()))
                .build()))
            .build();
    }

    private ChatbotResponseDTO fallbackResponse(ChatbotLanguage language) {
        String lang = language.code();
        return ChatbotResponseDTO.builder()
            .reply(text(lang,
                "Mình chưa hiểu rõ ý bạn. Bạn có thể hỏi về cách dùng web, truyện hot, truyện hoàn thành hoặc truyện mới cập nhật.",
                "I don't fully understand yet. You can ask about site help, popular novels, completed novels, or latest updates."))
            .language(lang)
            .intent("fallback")
            .confidence(0.0)
            .novels(List.of())
            .suggestedQuestions(knowledgeBase.suggestions(lang))
            .actions(List.of())
            .build();
    }

    private String text(String language, String vi, String en) {
        return "en".equals(language) ? en : vi;
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
}
