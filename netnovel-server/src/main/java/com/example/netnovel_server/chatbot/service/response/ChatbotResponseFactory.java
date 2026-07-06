package com.example.netnovel_server.chatbot.service.response;

import com.example.netnovel_server.chatbot.dto.ChatbotActionDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotResponseDTO;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotIntentAction;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.service.ChatbotKnowledgeBase;
import com.example.netnovel_server.chatbot.service.novel.ChatbotNovelSearchService;
import com.example.netnovel_server.dto.NovelDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class ChatbotResponseFactory {

    private final ChatbotKnowledgeBase knowledgeBase;
    private final ChatbotNovelSearchService novelSearchService;
    private final ChatbotActionFactory actionFactory;
    private final ChatbotSearchAccessPolicy searchAccessPolicy;

    public ChatbotResponseFactory(
        ChatbotKnowledgeBase knowledgeBase,
        ChatbotNovelSearchService novelSearchService,
        ChatbotActionFactory actionFactory,
        ChatbotSearchAccessPolicy searchAccessPolicy
    ) {
        this.knowledgeBase = knowledgeBase;
        this.novelSearchService = novelSearchService;
        this.actionFactory = actionFactory;
        this.searchAccessPolicy = searchAccessPolicy;
    }

    public ChatbotResponseDTO faqResponse(ChatbotMatchResult match) {
        ChatbotFaq faq = match.faq();
        String language = match.language().code();
        return ChatbotResponseDTO.builder()
            .reply(faq.answers().getOrDefault(language, faq.answers().get("vi")))
            .language(language)
            .intent(faq.id())
            .confidence(match.confidence())
            .novels(List.of())
            .suggestedQuestions(knowledgeBase.suggestions(language))
            .actions(actionFactory.faqActions(faq.actionUrls(), language))
            .build();
    }

    public ChatbotResponseDTO novelSearchResponse(ChatbotMatchResult match) {
        String language = match.language().code();
        ChatbotSearchAccessDecision searchAccess = searchAccessPolicy.decide(match.filters());
        List<NovelDTO> novels = novelSearchService.search(searchAccess.searchFilters());

        String reply = searchAccess.publicFallback()
            ? publicFallbackSearchText(language, match.filters().get("tag"))
            : novels.isEmpty()
            ? text(language,
                "Mình chưa tìm thấy truyện phù hợp. Bạn thử đổi thể loại hoặc từ khóa nhé.",
                "I couldn't find matching novels yet. Try another genre or keyword.")
            : switch (match.intent()) {
                case "popular_novels" -> text(language,
                    "Đây là một số truyện nổi bật cho bạn.",
                    "Here are some popular novels for you.");
                case "latest_novels" -> text(language,
                    "Đây là một số truyện mới cập nhật.",
                    "Here are some recently updated novels.");
                default -> text(language,
                    "Mình tìm thấy một số truyện phù hợp cho bạn.",
                    "I found some novels that may fit your request.");
            };

        return ChatbotResponseDTO.builder()
            .reply(reply)
            .language(language)
            .intent(match.intent())
            .confidence(match.confidence())
            .novels(novels)
            .suggestedQuestions(knowledgeBase.suggestions(language))
            .actions(List.of(actionFactory.searchResultsAction(
                searchAccess.actionFilters(),
                language,
                searchAccess.advancedMode()
            )))
            .build();
    }

    public ChatbotResponseDTO navigationResponse(ChatbotMatchResult match) {
        ChatbotIntent intent = match.intentDefinition();
        String language = match.language().code();
        var actions = actionFactory.navigationActions(intent, language);

        String reply = actions.isEmpty() && hasRestrictedActions(intent)
            ? navigationPermissionDeniedText(language, requiredRoles(intent))
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

    public ChatbotResponseDTO clarificationResponse(ChatbotMatchResult match) {
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
            actions.add(actionFactory.collectionAction(language));
        } else if ("search".equals(type)) {
            reply = text(language,
                "Bạn muốn mình tìm theo tiêu chí nào?",
                "How would you like me to search?");
            suggestions = knowledgeBase.suggestions(language);
            actions.add(actionFactory.searchAction(language));
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

    public ChatbotResponseDTO fallbackResponse(ChatbotLanguage language) {
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

    private String publicFallbackSearchText(String language, String tag) {
        String safeTag = tag == null || tag.isBlank() ? text(language, "tag này", "this tag") : tag;
        return text(language,
            "Mình nhận ra bạn muốn tìm theo tag \"" + safeTag + "\", nhưng lọc theo tag thuộc Advanced Search dành cho Manager/Admin. Mình sẽ chuyển bạn sang tìm kiếm công khai bằng từ khóa này trước nhé.",
            "I understand you want to search by the \"" + safeTag + "\" tag, but tag filtering belongs to Advanced Search for Manager/Admin. I'll open public keyword search for you instead.");
    }

    private boolean hasRestrictedActions(ChatbotIntent intent) {
        return intent.actions() != null && intent.actions().stream()
            .anyMatch(action -> action.requiredRoles() != null && !action.requiredRoles().isEmpty());
    }

    private Set<String> requiredRoles(ChatbotIntent intent) {
        Set<String> roles = new LinkedHashSet<>();
        if (intent.actions() == null) {
            return roles;
        }

        for (ChatbotIntentAction action : intent.actions()) {
            if (action.requiredRoles() != null) {
                roles.addAll(action.requiredRoles());
            }
        }
        return roles;
    }

    private String navigationPermissionDeniedText(String language, Set<String> roles) {
        String roleText = roles.isEmpty() ? text(language, "vai trò phù hợp", "a suitable role") : String.join("/", roles);
        return text(language,
            "Mình hiểu bạn muốn mở trang này, nhưng tài khoản của bạn chưa có quyền truy cập. Chức năng này yêu cầu " + roleText + ".",
            "I understand you want to open this page, but your account does not have permission to access it. This feature requires " + roleText + ".");
    }
}
