package com.example.netnovel_server.controller;

import com.example.netnovel_server.config.SecurityConfig;
import com.example.netnovel_server.dto.ElasticReindexResponseDTO;
import com.example.netnovel_server.dto.NotificationDTO;
import com.example.netnovel_server.dto.UserDTO;
import com.example.netnovel_server.dto.UserEventDataReportDTO;
import com.example.netnovel_server.entity.UserEventType;
import com.example.netnovel_server.recommendation.dto.UserNovelInteractionRebuildDTO;
import com.example.netnovel_server.recommendation.service.RecommendationAnalyticsService;
import com.example.netnovel_server.recommendation.service.UserNovelInteractionAggregationService;
import com.example.netnovel_server.search.elastic.service.ElasticAdminNovelSearchService;
import com.example.netnovel_server.search.elastic.service.ElasticDiagnosticsService;
import com.example.netnovel_server.search.elastic.service.ElasticNovelSearchIndexer;
import com.example.netnovel_server.search.elastic.service.ElasticSemanticNovelSearchService;
import com.example.netnovel_server.security.CustomUserDetailsService;
import com.example.netnovel_server.service.AdminUserService;
import com.example.netnovel_server.service.JwtService;
import com.example.netnovel_server.service.NotificationService;
import com.example.netnovel_server.service.UserEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
    AdminUserController.class,
    AdvancedSearchController.class,
    DataReportController.class,
    NotificationController.class
})
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class AdminAdvancedSecurityWebMvcTest {

    /*
     * Security MVC slice scope for admin and operational APIs:
     * - Loads real SecurityConfig and method security annotations.
     * - Uses @WithMockUser for role decisions.
     * - Mocks operational services so tests assert access control and endpoint wiring only.
     * - Elasticsearch ObjectProvider dependencies are satisfied by mocked beans, not a real cluster.
     */

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private RecommendationAnalyticsService recommendationAnalyticsService;

    @MockitoBean
    private UserNovelInteractionAggregationService userNovelInteractionAggregationService;

    @MockitoBean
    private ElasticNovelSearchIndexer elasticNovelSearchIndexer;

    @MockitoBean
    private ElasticAdminNovelSearchService elasticAdminNovelSearchService;

    @MockitoBean
    private ElasticSemanticNovelSearchService elasticSemanticNovelSearchService;

    @MockitoBean
    private ElasticDiagnosticsService elasticDiagnosticsService;

    @MockitoBean
    private UserEventService userEventService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void adminUsersRejectRegularUser() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isForbidden());

        verify(adminUserService, never()).getUsers(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminUsersAllowAdmin() throws Exception {
        when(adminUserService.getUsers(any())).thenReturn(new PageImpl<>(
            List.of(user()),
            PageRequest.of(0, 10),
            1
        ));

        mockMvc.perform(get("/api/admin/users?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].userId").value(7))
            .andExpect(jsonPath("$.content[0].roles[0]").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void userEventReportAllowsManager() throws Exception {
        when(recommendationAnalyticsService.getUserEventReport(14)).thenReturn(userEventReport());

        mockMvc.perform(get("/api/data-reports/user-events?days=14"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.periodDays").value(14))
            .andExpect(jsonPath("$.totalEvents").value(12));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void rebuildUserNovelInteractionsRejectsManager() throws Exception {
        mockMvc.perform(post("/api/data-reports/user-novel-interactions/rebuild"))
            .andExpect(status().isForbidden());

        verify(userNovelInteractionAggregationService, never()).rebuild();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rebuildUserNovelInteractionsAllowsAdmin() throws Exception {
        when(userNovelInteractionAggregationService.rebuild()).thenReturn(
            new UserNovelInteractionRebuildDTO(5, LocalDateTime.parse("2026-07-05T12:00:00"))
        );

        mockMvc.perform(post("/api/data-reports/user-novel-interactions/rebuild"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.interactionCount").value(5));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void advancedSearchReindexRejectsManager() throws Exception {
        mockMvc.perform(post("/api/advanced/search/reindex/novels"))
            .andExpect(status().isForbidden());

        verify(elasticNovelSearchIndexer, never()).reindexAllNovels();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void advancedSearchReindexAllowsAdmin() throws Exception {
        when(elasticNovelSearchIndexer.reindexAllNovels()).thenReturn(ElasticReindexResponseDTO.builder()
            .indexName("novels")
            .indexed(10)
            .failed(0)
            .build());

        mockMvc.perform(post("/api/advanced/search/reindex/novels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.indexName").value("novels"))
            .andExpect(jsonPath("$.indexed").value(10));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void sendNotificationToUserRejectsManager() throws Exception {
        mockMvc.perform(post("/api/notifications/users/7")
                .contentType("application/json")
                .content("{\"title\":\"Notice\",\"message\":\"Hello\"}"))
            .andExpect(status().isForbidden());

        verify(notificationService, never()).sendAdminNotificationToUser(eq(7L), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendNotificationToUserAllowsAdmin() throws Exception {
        when(notificationService.sendAdminNotificationToUser(eq(7L), any())).thenReturn(notification());

        mockMvc.perform(post("/api/notifications/users/7")
                .contentType("application/json")
                .content("{\"title\":\"Notice\",\"message\":\"Hello\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notificationId").value(50))
            .andExpect(jsonPath("$.title").value("Notice"));
    }

    private static UserDTO user() {
        return UserDTO.builder()
            .userId(7L)
            .username("admin")
            .email("admin@example.test")
            .roles(new String[] {"ADMIN"})
            .provider("LOCAL")
            .build();
    }

    private static UserEventDataReportDTO userEventReport() {
        return new UserEventDataReportDTO(
            14,
            LocalDateTime.parse("2026-06-21T00:00:00"),
            LocalDateTime.parse("2026-07-05T00:00:00"),
            12,
            Map.of(UserEventType.SEARCH, 4L),
            3,
            4,
            2,
            1,
            2,
            1,
            2.5,
            1.5
        );
    }

    private static NotificationDTO notification() {
        return NotificationDTO.builder()
            .notificationId(50L)
            .userId(7L)
            .type("ADMIN")
            .title("Notice")
            .message("Hello")
            .isRead(false)
            .createdAt(LocalDateTime.parse("2026-07-05T12:00:00"))
            .build();
    }
}
