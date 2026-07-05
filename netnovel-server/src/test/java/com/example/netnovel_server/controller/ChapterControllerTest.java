package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.ChapterContentDTO;
import com.example.netnovel_server.dto.ChapterDTO;
import com.example.netnovel_server.service.ChapterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChapterControllerTest {

    /*
     * Controller contract scope:
     * - Standalone MockMvc keeps this focused on route mapping and request/response JSON.
     * - ChapterService is mocked; service-level business rules are covered in ChapterServiceTest.
     */

    @Mock
    private ChapterService chapterService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ChapterController(chapterService))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void getAllChaptersReturnsChapterSummaries() throws Exception {
        when(chapterService.getAllChapters(10L)).thenReturn(List.of(chapterSummary()));

        mockMvc.perform(get("/api/novels/10/chapters/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].chapterId").value(5))
            .andExpect(jsonPath("$[0].title").value("Chapter One"));
    }

    @Test
    void getChapterReturnsChapterContent() throws Exception {
        when(chapterService.getChapter(5L)).thenReturn(chapterContent());

        mockMvc.perform(get("/api/chapters/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chapterId").value(5))
            .andExpect(jsonPath("$.content").value("Long chapter content."));
    }

    @Test
    void createChapterPostsPayloadToService() throws Exception {
        when(chapterService.createChapter(argThat(id -> id == 10L), argThat(request ->
            "Chapter One".equals(request.getTitle())
                && request.getChapterNumber() == 1
                && "Long chapter content.".equals(request.getContent())
        ))).thenReturn(chapterContent());

        mockMvc.perform(post("/api/novels/10/chapters")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Chapter One\",\"chapterNumber\":1,\"content\":\"Long chapter content.\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chapterNumber").value(1));
    }

    @Test
    void updateChapterPutsPayloadToService() throws Exception {
        when(chapterService.updateChapter(argThat(id -> id == 5L), argThat(request ->
            "Updated Chapter".equals(request.getTitle()) && request.getChapterNumber() == 2
        ))).thenReturn(ChapterContentDTO.builder()
            .chapterId(5L)
            .novelId(10L)
            .title("Updated Chapter")
            .chapterNumber(2)
            .content("Updated chapter content.")
            .build());

        mockMvc.perform(put("/api/chapters/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Chapter\",\"chapterNumber\":2,\"content\":\"Updated chapter content.\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Chapter"));
    }

    @Test
    void deleteChapterReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/chapters/5"))
            .andExpect(status().isNoContent());

        verify(chapterService).deleteChapter(5L);
    }

    private static ChapterDTO chapterSummary() {
        return ChapterDTO.builder()
            .chapterId(5L)
            .novelId(10L)
            .title("Chapter One")
            .chapterNumber(1)
            .build();
    }

    private static ChapterContentDTO chapterContent() {
        return ChapterContentDTO.builder()
            .chapterId(5L)
            .novelId(10L)
            .title("Chapter One")
            .chapterNumber(1)
            .content("Long chapter content.")
            .build();
    }
}
