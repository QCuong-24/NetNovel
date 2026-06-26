package com.example.netnovel_crawler.service;

import com.example.netnovel_crawler.entity.CrawlTaskStatus;
import com.example.netnovel_crawler.repository.CrawlTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RunningCrawlTaskRecovery {

    private static final Logger log = LoggerFactory.getLogger(RunningCrawlTaskRecovery.class);
    private static final String CANCEL_REASON = "Crawler was stopped or restarted before this task finished.";

    private final CrawlTaskRepository crawlTaskRepository;

    public RunningCrawlTaskRecovery(CrawlTaskRepository crawlTaskRepository) {
        this.crawlTaskRepository = crawlTaskRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void cancelInterruptedRunningTasks() {
        int cancelledCount = crawlTaskRepository.cancelRunningTasks(
            CrawlTaskStatus.RUNNING,
            CrawlTaskStatus.CANCELLED,
            LocalDateTime.now(),
            CANCEL_REASON
        );

        if (cancelledCount > 0) {
            log.warn("Cancelled interrupted crawl tasks on crawler startup. count={}", cancelledCount);
            return;
        }

        log.info("No interrupted running crawl tasks found on crawler startup.");
    }
}
