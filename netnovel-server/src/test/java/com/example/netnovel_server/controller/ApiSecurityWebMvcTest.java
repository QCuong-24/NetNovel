package com.example.netnovel_server.controller;

import com.example.netnovel_server.config.SecurityConfig;
import com.example.netnovel_server.dto.BookmarkDTO;
import com.example.netnovel_server.dto.CrawlChapterRecordDTO;
import com.example.netnovel_server.dto.CrawlTaskDTO;
import com.example.netnovel_server.dto.TagDTO;
import com.example.netnovel_server.entity.CrawlChapterStatus;
import com.example.netnovel_server.entity.CrawlTaskStatus;
import com.example.netnovel_server.security.CustomUserDetailsService;
import com.example.netnovel_server.service.BookmarkService;
import com.example.netnovel_server.service.CrawlTaskService;
import com.example.netnovel_server.service.JwtService;
import com.example.netnovel_server.service.TagService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {BookmarkController.class, TagController.class, CrawlTaskController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class ApiSecurityWebMvcTest {

    /*
     * Security MVC slice scope for authenticated and role-based APIs:
     * - Loads the real SecurityConfig/filter chain.
     * - Uses @WithMockUser to model USER, MANAGER, and ADMIN access.
     * - Services are mocked so assertions stay focused on access control and endpoint wiring.
     */

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookmarkService bookmarkService;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private CrawlTaskService crawlTaskService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void bookmarkListRejectsAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/bookmarks"))
            .andExpect(status().isForbidden());

        verify(bookmarkService, never()).getMyBookmarks(any());
    }

    @Test
    @WithMockUser
    void bookmarkListAllowsAuthenticatedUser() throws Exception {
        when(bookmarkService.getMyBookmarks(any())).thenReturn(new PageImpl<>(
            List.of(bookmark()),
            PageRequest.of(0, 10),
            1
        ));

        mockMvc.perform(get("/api/bookmarks?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].bookmarkId").value(100))
            .andExpect(jsonPath("$.content[0].novelTitle").value("Bookmarked Novel"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void tagsRejectRegularUser() throws Exception {
        mockMvc.perform(get("/api/tags"))
            .andExpect(status().isForbidden());

        verify(tagService, never()).getAllTags();
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void tagsAllowManager() throws Exception {
        when(tagService.getAllTags()).thenReturn(List.of(tag()));

        mockMvc.perform(get("/api/tags"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].tagId").value(1))
            .andExpect(jsonPath("$[0].name").value("Cultivation"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void crawlTasksRejectRegularUser() throws Exception {
        mockMvc.perform(get("/api/crawl-tasks"))
            .andExpect(status().isForbidden());

        verify(crawlTaskService, never()).getTasks(any(), eq(false), any());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void crawlTasksAllowManager() throws Exception {
        when(crawlTaskService.getTasks(eq(CrawlTaskStatus.PENDING), eq(false), any())).thenReturn(new PageImpl<>(
            List.of(crawlTask()),
            PageRequest.of(0, 5),
            1
        ));

        mockMvc.perform(get("/api/crawl-tasks?status=PENDING&page=0&size=5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(200))
            .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void crawlChapterRecordsRejectManager() throws Exception {
        mockMvc.perform(get("/api/crawl-tasks/crawl-chapter-records"))
            .andExpect(status().isForbidden());

        verify(crawlTaskService, never()).getCrawlChapterRecords(any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crawlChapterRecordsAllowAdmin() throws Exception {
        when(crawlTaskService.getCrawlChapterRecords(eq(CrawlChapterStatus.SUCCESS), eq(10L), any(), any(), any()))
            .thenReturn(new PageImpl<>(
                List.of(crawlChapterRecord()),
                PageRequest.of(1, 5),
                6
            ));

        mockMvc.perform(get("/api/crawl-tasks/crawl-chapter-records?status=SUCCESS&novelId=10&page=1&size=5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(300))
            .andExpect(jsonPath("$.content[0].status").value("SUCCESS"))
            .andExpect(jsonPath("$.number").value(1));
    }

    private static BookmarkDTO bookmark() {
        return BookmarkDTO.builder()
            .bookmarkId(100L)
            .userId(7L)
            .novelId(10L)
            .novelTitle("Bookmarked Novel")
            .author("Author")
            .build();
    }

    private static TagDTO tag() {
        return TagDTO.builder()
            .tagId(1L)
            .name("Cultivation")
            .build();
    }

    private static CrawlTaskDTO crawlTask() {
        return CrawlTaskDTO.builder()
            .id(200L)
            .url("https://example.test/novel")
            .status("PENDING")
            .requestedByUserId(7L)
            .createAt(LocalDateTime.parse("2026-07-05T10:00:00"))
            .build();
    }

    private static CrawlChapterRecordDTO crawlChapterRecord() {
        return CrawlChapterRecordDTO.builder()
            .id(300L)
            .sourceName("wuxiaworld")
            .sourceChapterUrl("https://example.test/chapter")
            .novelId(10L)
            .novelTitle("Novel")
            .chapterId(20L)
            .chapterTitle("Chapter One")
            .chapterNumber(1)
            .status("SUCCESS")
            .crawledAt(LocalDateTime.parse("2026-07-05T11:00:00"))
            .build();
    }
}
