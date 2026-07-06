package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.dto.ChatbotAdminTestResponseDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotAdminTestRequestDTO;
import com.example.netnovel_server.chatbot.dto.ChatbotResponseDTO;
import com.example.netnovel_server.chatbot.model.ChatbotIntentAction;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.service.embedding.ChatbotSemanticMatcher;
import com.example.netnovel_server.chatbot.service.language.ChatbotLanguageDetector;
import com.example.netnovel_server.chatbot.service.matching.ChatbotIntentMatcher;
import com.example.netnovel_server.chatbot.service.response.ChatbotResponseFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
public class ChatbotAdminTestService {

    private final ChatbotLanguageDetector languageDetector;
    private final ChatbotIntentMatcher intentMatcher;
    private final ChatbotSemanticMatcher semanticMatcher;
    private final ChatbotResponseFactory responseFactory;

    public ChatbotAdminTestService(
        ChatbotLanguageDetector languageDetector,
        ChatbotIntentMatcher intentMatcher,
        ChatbotSemanticMatcher semanticMatcher,
        ChatbotResponseFactory responseFactory
    ) {
        this.languageDetector = languageDetector;
        this.intentMatcher = intentMatcher;
        this.semanticMatcher = semanticMatcher;
        this.responseFactory = responseFactory;
    }

    public ChatbotAdminTestResponseDTO test(ChatbotAdminTestRequestDTO request) {
        String message = request != null ? request.getMessage() : "";
        String requestedLanguage = request != null ? request.getLanguage() : null;
        List<String> testRoles = normalizedRoles(request == null ? null : request.getRoles());
        Authentication originalAuthentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            applyTestRoles(testRoles);
            ChatbotLanguage language = languageDetector.detect(message, requestedLanguage);
            ChatbotMatchResult ruleBasedMatch = intentMatcher.match(message, language);
            boolean semanticTried = shouldTrySemantic(ruleBasedMatch);
            ChatbotMatchResult finalMatch = semanticTried
                ? semanticMatcher.match(message, language).orElse(ruleBasedMatch)
                : ruleBasedMatch;
            boolean semanticUsed = semanticTried && finalMatch != ruleBasedMatch;
            ChatbotResponseDTO response = responseFor(finalMatch);
            String responseType = responseType(finalMatch, response);

            return ChatbotAdminTestResponseDTO.builder()
                .message(message)
                .requestedLanguage(requestedLanguage)
                .testRoles(testRoles)
                .detectedLanguage(language.code())
                .intent(finalMatch.intent())
                .confidence(finalMatch.confidence())
                .secondBestConfidence(finalMatch.secondBestConfidence())
                .ambiguous(finalMatch.ambiguous())
                .clarificationType(finalMatch.clarificationType())
                .filters(finalMatch.filters())
                .faqId(finalMatch.faq() == null ? null : finalMatch.faq().id())
                .intentType(finalMatch.intentDefinition() == null ? null : finalMatch.intentDefinition().type())
                .responseType(responseType)
                .semanticTried(semanticTried)
                .semanticUsed(semanticUsed)
                .permissionDenied("permission_denied".equals(responseType))
                .searchMode(searchMode(response))
                .novelResultCount(response.getNovels() == null ? null : response.getNovels().size())
                .response(response)
                .actionValues(response.getActions() == null ? java.util.List.of() : response.getActions().stream().map(action -> action.getValue()).toList())
                .build();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
        }
    }

    private List<String> normalizedRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of("USER");
        }
        return roles.stream()
            .filter(role -> role != null && !role.isBlank())
            .map(role -> role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role)
            .distinct()
            .toList();
    }

    private void applyTestRoles(List<String> roles) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            "chatbot-admin-test-user",
            "n/a",
            roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .toList()
        ));
    }

    private ChatbotResponseDTO responseFor(ChatbotMatchResult match) {
        if (match.ambiguous()) {
            return responseFactory.clarificationResponse(match);
        }
        if ("faq".equals(match.intent()) && match.faq() != null) {
            return responseFactory.faqResponse(match);
        }
        if (match.intentDefinition() != null && "navigation".equals(match.intentDefinition().type())) {
            return responseFactory.navigationResponse(match);
        }
        if (match.confidence() >= 0.45 && isNovelSearchIntent(match)) {
            return responseFactory.novelSearchResponse(match);
        }
        return responseFactory.fallbackResponse(match.language());
    }

    private boolean isNovelSearchIntent(ChatbotMatchResult match) {
        return match.intent().contains("novel") || "search_by_title".equals(match.intent());
    }

    private boolean shouldTrySemantic(ChatbotMatchResult match) {
        if (match.ambiguous()) {
            return true;
        }
        if ("fallback".equals(match.intent())) {
            return true;
        }
        if ("faq".equals(match.intent()) && match.confidence() < 0.7) {
            return true;
        }
        return match.confidence() < 0.55 && match.filters().isEmpty();
    }

    private String responseType(ChatbotMatchResult match, ChatbotResponseDTO response) {
        if (match.ambiguous()) {
            return "clarification";
        }
        if ("faq".equals(match.intent())) {
            return "faq";
        }
        if (match.intentDefinition() != null && "navigation".equals(match.intentDefinition().type())) {
            return response.getActions().isEmpty() && hasRestrictedActions(match) ? "permission_denied" : "navigation";
        }
        if ("fallback".equals(match.intent())) {
            return "fallback";
        }
        if (isNovelSearchIntent(match)) {
            return "novel_search";
        }
        return "fallback";
    }

    private boolean hasRestrictedActions(ChatbotMatchResult match) {
        return match.intentDefinition() != null
            && match.intentDefinition().actions() != null
            && match.intentDefinition().actions().stream()
                .map(ChatbotIntentAction::requiredRoles)
                .anyMatch(roles -> roles != null && !roles.isEmpty());
    }

    private String searchMode(ChatbotResponseDTO response) {
        if (response.getActions() == null || response.getActions().isEmpty()) {
            return "none";
        }
        String actionValue = response.getActions().getFirst().getValue();
        if (actionValue == null) {
            return "none";
        }
        if (actionValue.endsWith("#advanced")) {
            return "advanced";
        }
        if (actionValue.endsWith("#public")) {
            return "public";
        }
        return "none";
    }
}
