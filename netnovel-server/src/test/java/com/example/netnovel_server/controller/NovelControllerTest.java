package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.service.NovelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NovelControllerTest {

    /*
     * Controller contract scope:
     * - Verifies public novel routes and management routes without loading security filters.
     * - NovelService is mocked; validation and relationship resolution live in NovelServiceTest.
     */

    @Mock
    private NovelService novelService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new NovelController(novelService))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void getNovelsReturnsPagedNovels() throws Exception {
        when(novelService.getNovels(any())).thenReturn(new PageImpl<>(
            List.of(novel()),
            PageRequest.of(2, 5),
            11
        ));

        mockMvc.perform(get("/api/novels?page=2&size=5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].novelId").value(10))
            .andExpect(jsonPath("$.number").value(2))
            .andExpect(jsonPath("$.size").value(5))
            .andExpect(jsonPath("$.totalElements").value(11));

        verify(novelService).getNovels(argThat(pageable ->
            pageable.getPageNumber() == 2 && pageable.getPageSize() == 5
        ));
    }

    @Test
    void searchByTitlePassesTitleAndPageableToService() throws Exception {
        when(novelService.searchByTitle(eq("net"), any())).thenReturn(new PageImpl<>(
            List.of(novel()),
            PageRequest.of(1, 10),
            1
        ));

        mockMvc.perform(get("/api/novels/search?title=net&page=1&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("NetNovel Sample"));

        verify(novelService).searchByTitle(eq("net"), argThat(pageable ->
            pageable.getPageNumber() == 1 && pageable.getPageSize() == 10
        ));
    }

    @Test
    void getLatestUpdatedNovelsReturnsPagedNovels() throws Exception {
        when(novelService.getLatestUpdatedNovels(any())).thenReturn(new PageImpl<>(
            List.of(novel()),
            PageRequest.of(0, 3),
            1
        ));

        mockMvc.perform(get("/api/novels/latest-updates?page=0&size=3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].novelId").value(10));

        verify(novelService).getLatestUpdatedNovels(argThat(pageable ->
            pageable.getPageNumber() == 0 && pageable.getPageSize() == 3
        ));
    }

    @Test
    void getCompletedNovelsReturnsPagedNovels() throws Exception {
        when(novelService.getCompletedNovels(any())).thenReturn(new PageImpl<>(
            List.of(novel()),
            PageRequest.of(0, 4),
            1
        ));

        mockMvc.perform(get("/api/novels/completed?page=0&size=4"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].status").value("ONGOING"));

        verify(novelService).getCompletedNovels(argThat(pageable ->
            pageable.getPageNumber() == 0 && pageable.getPageSize() == 4
        ));
    }

    @Test
    void getUpdatedNovelsParsesDateRangeAndPageable() throws Exception {
        LocalDateTime start = LocalDateTime.parse("2026-07-01T00:00:00");
        LocalDateTime end = LocalDateTime.parse("2026-07-05T23:59:59");

        when(novelService.getUpdatedNovels(eq(start), eq(end), any())).thenReturn(new PageImpl<>(
            List.of(novel()),
            PageRequest.of(0, 2),
            1
        ));

        mockMvc.perform(get("/api/novels/updated?start=2026-07-01T00:00:00&end=2026-07-05T23:59:59&page=0&size=2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("NetNovel Sample"));

        verify(novelService).getUpdatedNovels(eq(start), eq(end), argThat(pageable ->
            pageable.getPageNumber() == 0 && pageable.getPageSize() == 2
        ));
    }

    @Test
    void getNovelReturnsSingleNovel() throws Exception {
        when(novelService.getNovel(10L)).thenReturn(novel());

        mockMvc.perform(get("/api/novels/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.novelId").value(10))
            .andExpect(jsonPath("$.genres[0]").value("Fantasy"));
    }

    @Test
    void createNovelPostsPayloadToService() throws Exception {
        when(novelService.createNovel(argThat(request ->
            "NetNovel Sample".equals(request.getTitle())
                && "Author".equals(request.getAuthor())
                && request.getGenres().contains("Fantasy")
        ))).thenReturn(novel());

        mockMvc.perform(post("/api/novels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"NetNovel Sample\",\"author\":\"Author\",\"description\":\"Description\",\"genres\":[\"Fantasy\"],\"tags\":[\"Cultivation\"],\"status\":\"ONGOING\",\"accessStatus\":\"NORMAL\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("NetNovel Sample"));
    }

    @Test
    void updateNovelPutsPayloadToService() throws Exception {
        when(novelService.updateNovel(argThat(id -> id == 10L), argThat(request ->
            "Updated Novel".equals(request.getTitle()) && "COMPLETED".equals(request.getStatus())
        ))).thenReturn(NovelDTO.builder()
            .novelId(10L)
            .title("Updated Novel")
            .author("Author")
            .description("Description")
            .genres(Set.of("Fantasy"))
            .status("COMPLETED")
            .accessStatus("NORMAL")
            .build());

        mockMvc.perform(put("/api/novels/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Novel\",\"author\":\"Author\",\"description\":\"Description\",\"genres\":[\"Fantasy\"],\"tags\":[],\"status\":\"COMPLETED\",\"accessStatus\":\"NORMAL\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void deleteNovelReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/novels/10"))
            .andExpect(status().isNoContent());

        verify(novelService).deleteNovel(10L);
    }

    private static NovelDTO novel() {
        return NovelDTO.builder()
            .novelId(10L)
            .title("NetNovel Sample")
            .author("Author")
            .description("Description")
            .genres(Set.of("Fantasy"))
            .status("ONGOING")
            .accessStatus("NORMAL")
            .chapterCount(0)
            .build();
    }
}
