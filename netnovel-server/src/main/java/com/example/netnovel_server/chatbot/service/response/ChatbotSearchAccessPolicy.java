package com.example.netnovel_server.chatbot.service.response;

import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Decides whether a chatbot novel-search result should use public search or advanced search.
 *
 * Important rule:
 * - Tag filtering belongs to Advanced Search only.
 * - Manager/Admin can receive the real advanced tag filter.
 * - Normal users get a clear explanation and a public keyword fallback, so the bot does not
 *   pretend that public search filtered by tag exactly.
 */
@Component
public class ChatbotSearchAccessPolicy {

    private static final String TAG_FILTER = "tag";
    private static final String QUERY_FILTER = "q";

    public ChatbotSearchAccessDecision decide(Map<String, String> filters) {
        Map<String, String> safeFilters = filters == null ? Map.of() : filters;
        boolean advancedRequired = hasValue(safeFilters.get(TAG_FILTER));
        boolean advancedAllowed = !advancedRequired || SecurityUtils.hasAnyRole("MANAGER", "ADMIN");

        if (!advancedRequired || advancedAllowed) {
            return new ChatbotSearchAccessDecision(
                advancedRequired,
                advancedAllowed,
                safeFilters,
                safeFilters
            );
        }

        Map<String, String> publicFilters = publicKeywordFallback(safeFilters);
        return new ChatbotSearchAccessDecision(
            true,
            false,
            publicFilters,
            publicFilters
        );
    }

    private Map<String, String> publicKeywordFallback(Map<String, String> filters) {
        Map<String, String> publicFilters = new LinkedHashMap<>(filters);
        String tag = publicFilters.remove(TAG_FILTER);
        if (!hasValue(publicFilters.get(QUERY_FILTER))) {
            publicFilters.put(QUERY_FILTER, tag);
        }
        return publicFilters;
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}
