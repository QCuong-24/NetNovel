package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.CrawlChapterRecordDTO;
import com.example.netnovel_server.dto.CrawlTaskCreateDTO;
import com.example.netnovel_server.dto.CrawlTaskDTO;
import com.example.netnovel_server.entity.CrawlChapterStatus;
import com.example.netnovel_server.entity.CrawlTaskStatus;
import com.example.netnovel_server.service.CrawlTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/crawl-tasks")
@Tag(name = "Crawl Tasks", description = "Crawl task management APIs")
public class CrawlTaskController {

    private final CrawlTaskService crawlTaskService;

    public CrawlTaskController(CrawlTaskService crawlTaskService) {
        this.crawlTaskService = crawlTaskService;
    }

    @PostMapping
    @Operation(summary = "Create a full novel crawl task")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CrawlTaskDTO> createTask(@RequestBody CrawlTaskCreateDTO request) {
        return ResponseEntity.ok(crawlTaskService.createNovelCrawlTask(request));
    }

    @GetMapping
    @Operation(summary = "Get crawl tasks")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<CrawlTaskDTO>> getTasks(
        @RequestParam(required = false) CrawlTaskStatus status,
        @RequestParam(defaultValue = "false") boolean personal,
        Pageable pageable
    ) {
        return ResponseEntity.ok(crawlTaskService.getTasks(status, personal, pageable));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get a crawl task by id")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CrawlTaskDTO> getTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(crawlTaskService.getTask(taskId));
    }

    @GetMapping("/crawl-chapter-records")
    @Operation(summary = "Get crawl chapter records")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CrawlChapterRecordDTO>> getCrawlChapterRecords(
        @RequestParam(required = false) CrawlChapterStatus status,
        @RequestParam(required = false) Long novelId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
        Pageable pageable
    ) {
        return ResponseEntity.ok(crawlTaskService.getCrawlChapterRecords(status, novelId, start, end, pageable));
    }

    @DeleteMapping("/crawl-chapter-records/{recordId}")
    @Operation(summary = "Delete a crawl chapter record")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCrawlChapterRecord(@PathVariable Long recordId) {
        crawlTaskService.deleteCrawlChapterRecord(recordId);
        return ResponseEntity.noContent().build();
    }
}
