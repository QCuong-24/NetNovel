package com.example.netnovel_server.chatbot.service;

import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.dto.ChatbotResponseDTO;
import com.example.netnovel_server.chatbot.service.language.ChatbotLanguageDetector;
import com.example.netnovel_server.chatbot.service.language.ChatbotTextNormalizer;
import com.example.netnovel_server.chatbot.service.matching.RuleBasedChatbotIntentMatcher;
import com.example.netnovel_server.chatbot.service.novel.ChatbotNovelSearchService;
import com.example.netnovel_server.chatbot.service.response.ChatbotActionFactory;
import com.example.netnovel_server.chatbot.service.response.ChatbotResponseFactory;
import com.example.netnovel_server.chatbot.service.response.ChatbotSearchAccessPolicy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatbotEvaluationMetricsTest {

    // Evaluation dataset runner for report metrics: language accuracy, intent accuracy,
    // expected filter accuracy, FAQ id accuracy, ambiguity accuracy, and fallback accuracy.

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatbotLanguageDetector languageDetector = new ChatbotLanguageDetector();
    private final RuleBasedChatbotIntentMatcher matcher = new RuleBasedChatbotIntentMatcher(
        new ChatbotKnowledgeBase(),
        new ChatbotTextNormalizer()
    );
    private final ChatbotNovelSearchService novelSearchService = mock(ChatbotNovelSearchService.class);
    private final ChatbotResponseFactory responseFactory = new ChatbotResponseFactory(
        new ChatbotKnowledgeBase(),
        novelSearchService,
        new ChatbotActionFactory(),
        new ChatbotSearchAccessPolicy()
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void evaluatesCuratedChatbotDataset() throws IOException {
        when(novelSearchService.search(anyMap())).thenReturn(List.of());
        List<EvaluationCase> cases = loadCases();
        EvaluationMetrics metrics = evaluate(cases);

        System.out.println(metrics.report());

        assertTrue(metrics.failures().isEmpty(), () -> String.join(System.lineSeparator(), metrics.failures()));
    }

    private List<EvaluationCase> loadCases() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader()
            .getResourceAsStream("chatbot/chatbot-evaluation-cases.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing chatbot evaluation dataset.");
            }
            return objectMapper.readValue(inputStream, new TypeReference<>() {});
        }
    }

    private EvaluationMetrics evaluate(List<EvaluationCase> cases) {
        int languageCorrect = 0;
        int intentCorrect = 0;
        int faqCorrect = 0;
        int faqTotal = 0;
        int ambiguousCorrect = 0;
        int ambiguousTotal = 0;
        int fallbackCorrect = 0;
        int fallbackTotal = 0;
        int filterCorrect = 0;
        int filterTotal = 0;
        int responseTypeCorrect = 0;
        int actionCorrect = 0;
        int actionTotal = 0;
        int searchModeCorrect = 0;
        int searchModeTotal = 0;
        int permissionSafetyCorrect = 0;
        int permissionSafetyTotal = 0;
        List<String> failures = new ArrayList<>();

        for (EvaluationCase evaluationCase : cases) {
            applyRoles(evaluationCase.roles());
            ChatbotLanguage expectedLanguage = ChatbotLanguage.valueOf(evaluationCase.expectedLanguage().toUpperCase());
            ChatbotLanguage detectedLanguage = languageDetector.detect(evaluationCase.message(), null);
            ChatbotMatchResult result = matcher.match(evaluationCase.message(), detectedLanguage);
            ChatbotResponseDTO response = responseFor(result);

            if (detectedLanguage == expectedLanguage) {
                languageCorrect++;
            } else {
                failures.add("Language mismatch for \"" + evaluationCase.message() + "\": expected="
                    + expectedLanguage + ", actual=" + detectedLanguage);
            }

            if (evaluationCase.expectedIntent().equals(result.intent())) {
                intentCorrect++;
            } else {
                failures.add("Intent mismatch for \"" + evaluationCase.message() + "\": expected="
                    + evaluationCase.expectedIntent() + ", actual=" + result.intent() + ", filters=" + result.filters());
            }

            if (evaluationCase.expectedFaqId() != null && !evaluationCase.expectedFaqId().isBlank()) {
                faqTotal++;
                String actualFaqId = result.faq() == null ? null : result.faq().id();
                if (evaluationCase.expectedFaqId().equals(actualFaqId)) {
                    faqCorrect++;
                } else {
                    failures.add("FAQ mismatch for \"" + evaluationCase.message() + "\": expected="
                        + evaluationCase.expectedFaqId() + ", actual=" + actualFaqId);
                }
            }

            if (evaluationCase.expectedAmbiguous() != null) {
                ambiguousTotal++;
                if (evaluationCase.expectedAmbiguous().equals(result.ambiguous())) {
                    ambiguousCorrect++;
                } else {
                    failures.add("Ambiguity mismatch for \"" + evaluationCase.message() + "\": expected="
                        + evaluationCase.expectedAmbiguous() + ", actual=" + result.ambiguous());
                }
            }

            if (evaluationCase.expectedFallback() != null) {
                fallbackTotal++;
                boolean actualFallback = "fallback".equals(result.intent());
                if (evaluationCase.expectedFallback().equals(actualFallback)) {
                    fallbackCorrect++;
                } else {
                    failures.add("Fallback mismatch for \"" + evaluationCase.message() + "\": expected="
                        + evaluationCase.expectedFallback() + ", actual=" + actualFallback + ", intent=" + result.intent());
                }
            }

            for (Map.Entry<String, String> expectedFilter : evaluationCase.expectedFilters().entrySet()) {
                filterTotal++;
                String actualValue = result.filters().get(expectedFilter.getKey());
                if (expectedFilter.getValue().equals(actualValue)) {
                    filterCorrect++;
                } else {
                    failures.add("Filter mismatch for \"" + evaluationCase.message() + "\" key="
                        + expectedFilter.getKey() + ": expected=" + expectedFilter.getValue() + ", actual=" + actualValue
                        + ", allFilters=" + result.filters());
                }
            }

            String expectedResponseType = evaluationCase.expectedResponseType() == null
                ? inferResponseType(evaluationCase.expectedIntent(), evaluationCase.expectedAmbiguous(), evaluationCase.expectedFallback(), evaluationCase.expectedPermissionDenied())
                : evaluationCase.expectedResponseType();
            String actualResponseType = responseType(result, response);
            if (expectedResponseType.equals(actualResponseType)) {
                responseTypeCorrect++;
            } else {
                failures.add("Response type mismatch for \"" + evaluationCase.message() + "\": expected="
                    + expectedResponseType + ", actual=" + actualResponseType + ", intent=" + result.intent());
            }

            if (evaluationCase.expectedActionValueContains() != null && !evaluationCase.expectedActionValueContains().isBlank()) {
                actionTotal++;
                String actualActionValue = response.getActions().isEmpty() ? null : response.getActions().getFirst().getValue();
                if (actualActionValue != null && actualActionValue.contains(evaluationCase.expectedActionValueContains())) {
                    actionCorrect++;
                } else {
                    failures.add("Action mismatch for \"" + evaluationCase.message() + "\": expected contains="
                        + evaluationCase.expectedActionValueContains() + ", actual=" + actualActionValue);
                }
            }

            if (evaluationCase.expectedSearchMode() != null && !evaluationCase.expectedSearchMode().isBlank()) {
                searchModeTotal++;
                String actionValue = response.getActions().isEmpty() ? "" : response.getActions().getFirst().getValue();
                String actualMode = actionValue.endsWith("#advanced") ? "advanced" : actionValue.endsWith("#public") ? "public" : "none";
                if (evaluationCase.expectedSearchMode().equals(actualMode)) {
                    searchModeCorrect++;
                } else {
                    failures.add("Search mode mismatch for \"" + evaluationCase.message() + "\": expected="
                        + evaluationCase.expectedSearchMode() + ", actual=" + actualMode + ", action=" + actionValue);
                }
            }

            if (evaluationCase.expectedPermissionDenied() != null) {
                permissionSafetyTotal++;
                boolean actualPermissionDenied = "permission_denied".equals(actualResponseType);
                if (evaluationCase.expectedPermissionDenied().equals(actualPermissionDenied)) {
                    permissionSafetyCorrect++;
                } else {
                    failures.add("Permission safety mismatch for \"" + evaluationCase.message() + "\": expected="
                        + evaluationCase.expectedPermissionDenied() + ", actual=" + actualPermissionDenied
                        + ", actions=" + response.getActions());
                }
            }
            SecurityContextHolder.clearContext();
        }

        return new EvaluationMetrics(
            cases.size(),
            languageCorrect,
            intentCorrect,
            filterCorrect,
            filterTotal,
            faqCorrect,
            faqTotal,
            ambiguousCorrect,
            ambiguousTotal,
            fallbackCorrect,
            fallbackTotal,
            responseTypeCorrect,
            cases.size(),
            actionCorrect,
            actionTotal,
            searchModeCorrect,
            searchModeTotal,
            permissionSafetyCorrect,
            permissionSafetyTotal,
            failures
        );
    }

    private void applyRoles(List<String> roles) {
        SecurityContextHolder.clearContext();
        if (roles == null || roles.isEmpty()) {
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            "evaluation-user",
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

    private String inferResponseType(String expectedIntent, Boolean expectedAmbiguous, Boolean expectedFallback, Boolean expectedPermissionDenied) {
        if (Boolean.TRUE.equals(expectedPermissionDenied)) {
            return "permission_denied";
        }
        if (Boolean.TRUE.equals(expectedAmbiguous) || expectedIntent.startsWith("clarify_")) {
            return "clarification";
        }
        if (Boolean.TRUE.equals(expectedFallback) || "fallback".equals(expectedIntent)) {
            return "fallback";
        }
        if ("faq".equals(expectedIntent)) {
            return "faq";
        }
        if (expectedIntent.startsWith("navigate_")) {
            return "navigation";
        }
        return "novel_search";
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
                .anyMatch(action -> action.requiredRoles() != null && !action.requiredRoles().isEmpty());
    }

    record EvaluationCase(
        String message,
        String expectedLanguage,
        String expectedIntent,
        Map<String, String> expectedFilters,
        String expectedFaqId,
        Boolean expectedAmbiguous,
        Boolean expectedFallback,
        String expectedResponseType,
        String expectedActionValueContains,
        String expectedSearchMode,
        Boolean expectedPermissionDenied,
        List<String> roles
    ) {
        EvaluationCase {
            expectedFilters = expectedFilters == null ? Map.of() : expectedFilters;
            roles = roles == null ? List.of() : roles;
        }
    }

    record EvaluationMetrics(
        int totalCases,
        int languageCorrect,
        int intentCorrect,
        int filterCorrect,
        int filterTotal,
        int faqCorrect,
        int faqTotal,
        int ambiguousCorrect,
        int ambiguousTotal,
        int fallbackCorrect,
        int fallbackTotal,
        int responseTypeCorrect,
        int responseTypeTotal,
        int actionCorrect,
        int actionTotal,
        int searchModeCorrect,
        int searchModeTotal,
        int permissionSafetyCorrect,
        int permissionSafetyTotal,
        List<String> failures
    ) {
        String report() {
            return System.lineSeparator()
                + "==== Chatbot evaluation metrics ====" + System.lineSeparator()
                + "Total cases: " + totalCases + System.lineSeparator()
                + "Language accuracy: " + percent(languageCorrect, totalCases) + " (" + languageCorrect + "/" + totalCases + ")" + System.lineSeparator()
                + "Intent accuracy: " + percent(intentCorrect, totalCases) + " (" + intentCorrect + "/" + totalCases + ")" + System.lineSeparator()
                + "Filter accuracy: " + percent(filterCorrect, filterTotal) + " (" + filterCorrect + "/" + filterTotal + ")" + System.lineSeparator()
                + "FAQ id accuracy: " + percent(faqCorrect, faqTotal) + " (" + faqCorrect + "/" + faqTotal + ")" + System.lineSeparator()
                + "Ambiguity accuracy: " + percent(ambiguousCorrect, ambiguousTotal) + " (" + ambiguousCorrect + "/" + ambiguousTotal + ")" + System.lineSeparator()
                + "Fallback accuracy: " + percent(fallbackCorrect, fallbackTotal) + " (" + fallbackCorrect + "/" + fallbackTotal + ")" + System.lineSeparator()
                + "Response type accuracy: " + percent(responseTypeCorrect, responseTypeTotal) + " (" + responseTypeCorrect + "/" + responseTypeTotal + ")" + System.lineSeparator()
                + "Action accuracy: " + percent(actionCorrect, actionTotal) + " (" + actionCorrect + "/" + actionTotal + ")" + System.lineSeparator()
                + "Search mode accuracy: " + percent(searchModeCorrect, searchModeTotal) + " (" + searchModeCorrect + "/" + searchModeTotal + ")" + System.lineSeparator()
                + "Permission safety rate: " + percent(permissionSafetyCorrect, permissionSafetyTotal) + " (" + permissionSafetyCorrect + "/" + permissionSafetyTotal + ")" + System.lineSeparator()
                + "Failures: " + failures.size() + System.lineSeparator()
                + "====================================";
        }

        private String percent(int numerator, int denominator) {
            if (denominator == 0) {
                return "N/A";
            }
            return String.format("%.2f%%", numerator * 100.0 / denominator);
        }
    }
}
