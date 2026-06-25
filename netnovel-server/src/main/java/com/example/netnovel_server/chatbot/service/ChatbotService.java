package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.dto.ChatbotActionDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotRequestDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotResponseDTO;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotIntentAction;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
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

        if (match.ambiguous()) {
            fallbackLogger.log(message, language, match.intent(), match.confidence(), match.filters(), null);
            return clarificationResponse(match);
        }

        if ("faq".equals(match.intent()) && match.faq() != null) {
            return faqResponse(match);
        }

        if (match.intentDefinition() != null && "navigation".equals(match.intentDefinition().type())) {
            return navigationResponse(match);
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

    private ChatbotResponseDTO navigationResponse(ChatbotMatchResult match) {
        ChatbotIntent intent = match.intentDefinition();
        String language = match.language().code();
        List<ChatbotActionDTO> actions = intent.actions() == null
            ? List.of()
            : intent.actions().stream()
                .filter(this::canUseAction)
                .map(action -> ChatbotActionDTO.builder()
                    .label(action.labels().getOrDefault(language, action.labels().getOrDefault("en", action.value())))
                    .type(action.type())
                    .value(action.value())
                    .build())
                .toList();

        String reply = actions.isEmpty() && "navigate_dashboard".equals(intent.id())
            ? text(language, "Tài khoản của bạn chưa có quyền mở dashboard.", "Your account does not have access to the dashboard.")
            : intent.replies().getOrDefault(language, intent.replies().getOrDefault("en", ""));

        return ChatbotResponseDTO.builder()
            .reply(reply)
            .language(language)
            .intent(intent.id())
            .confidence(match.confidence())
            .novels(List.of())
            .suggestedQuestions(knowledgeBase.suggestions(language))
            .actions(actions)
            .build();
    }

    private ChatbotResponseDTO clarificationResponse(ChatbotMatchResult match) {
        String language = match.language().code();
        String type = match.clarificationType();
        List<ChatbotActionDTO> actions = new ArrayList<>();
        List<String> suggestions;
        String reply;

        if ("save_novel".equals(type)) {
            reply = text(language,
                "Bạn muốn theo dõi truyện để nhận cập nhật, hay bookmark để đọc lại sau?",
                "Do you want to follow novels for updates, or bookmark them to read later?");
            suggestions = "en".equals(language)
                ? List.of("How to follow a novel?", "What is bookmark?", "Open my collection")
                : List.of("Cách theo dõi truyện", "Bookmark là gì", "Mở bộ sưu tập");
            actions.add(ChatbotActionDTO.builder()
                .label(text(language, "Mở bộ sưu tập", "Open collection"))
                .type("navigate")
                .value("/collection")
                .build());
        } else if ("search".equals(type)) {
            reply = text(language,
                "Bạn muốn mình tìm theo tiêu chí nào?",
                "How would you like me to search?");
            suggestions = knowledgeBase.suggestions(language);
            actions.add(ChatbotActionDTO.builder()
                .label(text(language, "Mở tìm kiếm", "Open search"))
                .type("navigate")
                .value("/search")
                .build());
        } else {
            reply = text(language,
                "Mình thấy câu này hơi mơ hồ. Bạn muốn tìm truyện hay hỏi cách dùng website?",
                "That sounds a bit ambiguous. Do you want to search for novels or ask about site features?");
            suggestions = knowledgeBase.suggestions(language);
        }

        return ChatbotResponseDTO.builder()
            .reply(reply)
            .language(language)
            .intent(match.intent())
            .confidence(match.confidence())
            .novels(List.of())
            .suggestedQuestions(suggestions)
            .actions(actions)
            .build();
    }

    private ChatbotResponseDTO fallbackResponse(ChatbotLanguage language) {
        String lang = language.code();
        return ChatbotResponseDTO.builder()
            .reply(text(lang,
                "Mình chưa hiểu rõ ý bạn. Bạn muốn tìm truyện, mở một trang, hay hỏi cách dùng website?",
                "I don't fully understand yet. Do you want to search for novels, open a page, or ask about site features?"))
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
}
