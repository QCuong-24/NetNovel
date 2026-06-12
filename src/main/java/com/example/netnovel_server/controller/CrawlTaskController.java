package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.CrawlTaskCreateDTO;
import com.example.netnovel_server.dto.CrawlTaskDTO;
import com.example.netnovel_server.entity.CrawlTaskStatus;
import com.example.netnovel_server.service.CrawlTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/crawl-tasks")
@Tag(name = "Crawl Tasks", description = "Admin crawl task APIs")
public class CrawlTaskController {

    private final CrawlTaskService crawlTaskService;

    public CrawlTaskController(CrawlTaskService crawlTaskService) {
        this.crawlTaskService = crawlTaskService;
    }

    @PostMapping
    @Operation(summary = "Create a full novel crawl task")
    public ResponseEntity<CrawlTaskDTO> createTask(@RequestBody CrawlTaskCreateDTO request) {
        return ResponseEntity.ok(crawlTaskService.createNovelCrawlTask(request));
    }

    @GetMapping
    @Operation(summary = "Get crawl tasks")
    public ResponseEntity<Page<CrawlTaskDTO>> getTasks(
        @RequestParam(required = false) CrawlTaskStatus status,
        Pageable pageable
    ) {
        return ResponseEntity.ok(crawlTaskService.getTasks(status, pageable));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get a crawl task by id")
    public ResponseEntity<CrawlTaskDTO> getTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(crawlTaskService.getTask(taskId));
    }
}
