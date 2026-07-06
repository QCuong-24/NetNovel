package com.example.netnovel_server.chatbot.service.response;

import com.example.netnovel_server.chatbot.dto.ChatbotResponseDTO;
import com.example.netnovel_server.chatbot.model.ChatbotFaq;
import com.example.netnovel_server.chatbot.model.ChatbotIntent;
import com.example.netnovel_server.chatbot.model.ChatbotIntentAction;
import com.example.netnovel_server.chatbot.model.ChatbotLanguage;
import com.example.netnovel_server.chatbot.model.ChatbotMatchResult;
import com.example.netnovel_server.chatbot.service.ChatbotKnowledgeBase;
import com.example.netnovel_server.chatbot.service.novel.ChatbotNovelSearchService;
import com.example.netnovel_server.dto.NovelDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotResponseFactoryTest {

    // Response factory coverage: final reply, suggestions, actions, search URL, and novel payload.

    @Mock
    private ChatbotKnowledgeBase knowledgeBase;

    @Mock
    private ChatbotNovelSearchService novelSearchService;

    private ChatbotResponseFactory responseFactory;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        responseFactory = new ChatbotResponseFactory(
            knowledgeBase,
            novelSearchService,
            new ChatbotActionFactory(),
            new ChatbotSearchAccessPolicy()
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void buildsFaqResponseWithLocalizedAnswerAndAction() {
        when(knowledgeBase.suggestions("vi")).thenReturn(List.of("Truyện hot"));
        ChatbotFaq faq = new ChatbotFaq(
            "how_to_follow_novel",
            "faq",
            true,
            5,
            Map.of(),
            Map.of("vi", "Bạn có thể theo dõi truyện ở trang chi tiết.", "en", "You can follow a novel from its detail page."),
            List.of("/collection"),
            List.of("follow")
        );
        ChatbotMatchResult match = new ChatbotMatchResult("faq", ChatbotLanguage.VI, 0.92, 0.0, false, null, Map.of(), faq, null);

        ChatbotResponseDTO response = responseFactory.faqResponse(match);

        assertEquals("Bạn có thể theo dõi truyện ở trang chi tiết.", response.getReply());
        assertEquals("vi", response.getLanguage());
        assertEquals("how_to_follow_novel", response.getIntent());
        assertEquals(List.of("Truyện hot"), response.getSuggestedQuestions());
        assertEquals(1, response.getActions().size());
        assertEquals("navigate", response.getActions().getFirst().getType());
        assertEquals("/collection", response.getActions().getFirst().getValue());
    }

    @Test
    void buildsNovelSearchResponseWithResultsAndSearchAction() {
        Map<String, String> filters = Map.of("q", "martial", "status", "COMPLETED");
        NovelDTO novel = NovelDTO.builder()
            .novelId(1L)
            .title("A Martial Story")
            .author("Author")
            .status("COMPLETED")
            .build();
        when(novelSearchService.search(filters)).thenReturn(List.of(novel));
        when(knowledgeBase.suggestions("en")).thenReturn(List.of("Popular novels"));
        ChatbotMatchResult match = new ChatbotMatchResult("filtered_novels", ChatbotLanguage.EN, 0.75, 0.0, false, null, filters, null, null);

        ChatbotResponseDTO response = responseFactory.novelSearchResponse(match);

        assertEquals("I found some novels that may fit your request.", response.getReply());
        assertEquals("filtered_novels", response.getIntent());
        assertEquals(1, response.getNovels().size());
        assertEquals("A Martial Story", response.getNovels().getFirst().getTitle());
        assertEquals("See more results", response.getActions().getFirst().getLabel());
        assertTrue(response.getActions().getFirst().getValue().startsWith("/search"));
        assertTrue(response.getActions().getFirst().getValue().contains("status=COMPLETED"));
        assertTrue(response.getActions().getFirst().getValue().contains("q=martial"));
        assertTrue(response.getActions().getFirst().getValue().endsWith("#public"));
    }

    @Test
    void downgradesTagSearchToPublicKeywordSearchForNormalUser() {
        Map<String, String> advancedFilters = Map.of("tag", "Cultivation", "status", "COMPLETED");
        Map<String, String> publicFilters = Map.of("status", "COMPLETED", "q", "Cultivation");
        NovelDTO novel = NovelDTO.builder()
            .novelId(2L)
            .title("Cultivation Keyword Result")
            .author("Author")
            .status("COMPLETED")
            .build();
        when(novelSearchService.search(publicFilters)).thenReturn(List.of(novel));
        when(knowledgeBase.suggestions("vi")).thenReturn(List.of("Truyện hot"));
        ChatbotMatchResult match = new ChatbotMatchResult(
            "filtered_novels",
            ChatbotLanguage.VI,
            0.75,
            0.0,
            false,
            null,
            advancedFilters,
            null,
            null
        );

        ChatbotResponseDTO response = responseFactory.novelSearchResponse(match);

        assertTrue(response.getReply().contains("lọc theo tag thuộc Advanced Search"));
        assertEquals(1, response.getNovels().size());
        assertEquals("Cultivation Keyword Result", response.getNovels().getFirst().getTitle());
        assertTrue(response.getActions().getFirst().getValue().contains("q=Cultivation"));
        assertTrue(response.getActions().getFirst().getValue().contains("status=COMPLETED"));
        assertTrue(response.getActions().getFirst().getValue().endsWith("#public"));
    }

    @Test
    void keepsTagSearchInAdvancedModeForManager() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            "7",
            "n/a",
            List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
        ));
        Map<String, String> filters = Map.of("tag", "Cultivation", "status", "COMPLETED");
        NovelDTO novel = NovelDTO.builder()
            .novelId(3L)
            .title("Exact Cultivation Tag Result")
            .author("Author")
            .status("COMPLETED")
            .build();
        when(novelSearchService.search(filters)).thenReturn(List.of(novel));
        when(knowledgeBase.suggestions("en")).thenReturn(List.of("Popular novels"));
        ChatbotMatchResult match = new ChatbotMatchResult("filtered_novels", ChatbotLanguage.EN, 0.75, 0.0, false, null, filters, null, null);

        ChatbotResponseDTO response = responseFactory.novelSearchResponse(match);

        assertEquals("I found some novels that may fit your request.", response.getReply());
        assertEquals(1, response.getNovels().size());
        assertEquals("Exact Cultivation Tag Result", response.getNovels().getFirst().getTitle());
        assertTrue(response.getActions().getFirst().getValue().contains("q=Cultivation"));
        assertTrue(response.getActions().getFirst().getValue().contains("status=COMPLETED"));
        assertTrue(response.getActions().getFirst().getValue().endsWith("#advanced"));
    }

    @Test
    void buildsNavigationResponseFromIntentActions() {
        when(knowledgeBase.suggestions("en")).thenReturn(List.of("Latest updates"));
        ChatbotIntentAction action = new ChatbotIntentAction(
            Map.of("en", "Open collection", "vi", "Mở bộ sưu tập"),
            "navigate",
            "/collection",
            List.of()
        );
        ChatbotIntent intent = new ChatbotIntent(
            "navigate_collection",
            "navigation",
            true,
            10,
            Map.of(),
            Map.of("en", "Opening your collection.", "vi", "Đang mở bộ sưu tập."),
            Map.of(),
            List.of(),
            List.of(action)
        );
        ChatbotMatchResult match = new ChatbotMatchResult(intent.id(), ChatbotLanguage.EN, 0.9, 0.0, false, null, Map.of(), null, intent);

        ChatbotResponseDTO response = responseFactory.navigationResponse(match);

        assertEquals("Opening your collection.", response.getReply());
        assertEquals("navigate_collection", response.getIntent());
        assertEquals("Open collection", response.getActions().getFirst().getLabel());
        assertEquals("/collection", response.getActions().getFirst().getValue());
    }

    @Test
    void returnsPermissionDeniedNavigationResponseWhenUserLacksRequiredRole() {
        when(knowledgeBase.suggestions("vi")).thenReturn(List.of("Truyện hot"));
        ChatbotIntentAction action = new ChatbotIntentAction(
            Map.of("vi", "Mở quản lý chatbot", "en", "Open chatbot manager"),
            "navigate",
            "/dashboard#chatbot",
            List.of("ADMIN")
        );
        ChatbotIntent intent = new ChatbotIntent(
            "navigate_admin_chatbot",
            "navigation",
            true,
            10,
            Map.of(),
            Map.of("vi", "Trang quản lý chatbot nằm trong dashboard.", "en", "Chatbot management is inside the dashboard."),
            Map.of(),
            List.of(),
            List.of(action)
        );
        ChatbotMatchResult match = new ChatbotMatchResult(intent.id(), ChatbotLanguage.VI, 0.9, 0.0, false, null, Map.of(), null, intent);

        ChatbotResponseDTO response = responseFactory.navigationResponse(match);

        assertEquals("navigate_admin_chatbot", response.getIntent());
        assertTrue(response.getReply().contains("chưa có quyền truy cập"));
        assertTrue(response.getReply().contains("ADMIN"));
        assertTrue(response.getActions().isEmpty());
    }

    @Test
    void returnsRestrictedNavigationActionWhenAdminHasRequiredRole() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            "9",
            "n/a",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
        when(knowledgeBase.suggestions("en")).thenReturn(List.of("Popular novels"));
        ChatbotIntentAction action = new ChatbotIntentAction(
            Map.of("vi", "Mở quản lý chatbot", "en", "Open chatbot manager"),
            "navigate",
            "/dashboard#chatbot",
            List.of("ADMIN")
        );
        ChatbotIntent intent = new ChatbotIntent(
            "navigate_admin_chatbot",
            "navigation",
            true,
            10,
            Map.of(),
            Map.of("vi", "Trang quản lý chatbot nằm trong dashboard.", "en", "Chatbot management is inside the dashboard."),
            Map.of(),
            List.of(),
            List.of(action)
        );
        ChatbotMatchResult match = new ChatbotMatchResult(intent.id(), ChatbotLanguage.EN, 0.9, 0.0, false, null, Map.of(), null, intent);

        ChatbotResponseDTO response = responseFactory.navigationResponse(match);

        assertEquals("Chatbot management is inside the dashboard.", response.getReply());
        assertEquals("Open chatbot manager", response.getActions().getFirst().getLabel());
        assertEquals("/dashboard#chatbot", response.getActions().getFirst().getValue());
    }

    @Test
    void buildsFallbackResponseInVietnamese() {
        when(knowledgeBase.suggestions("vi")).thenReturn(List.of("Truyện hot"));

        ChatbotResponseDTO response = responseFactory.fallbackResponse(ChatbotLanguage.VI);

        assertEquals("fallback", response.getIntent());
        assertEquals("vi", response.getLanguage());
        assertTrue(response.getReply().contains("Mình chưa hiểu rõ"));
        assertTrue(response.getActions().isEmpty());
    }
}
