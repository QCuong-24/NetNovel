package com.example.netnovel_crawler.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    prefix = "app.crawler.pending-recovery",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class PendingCrawlTaskScheduler {

    private final CrawlTaskWorker crawlTaskWorker;

    public PendingCrawlTaskScheduler(CrawlTaskWorker crawlTaskWorker) {
        this.crawlTaskWorker = crawlTaskWorker;
    }

    @Scheduled(
        fixedDelayString = "${app.crawler.pending-recovery.delay-ms:60000}",
        initialDelayString = "${app.crawler.pending-recovery.initial-delay-ms:30000}"
    )
    public void recoverOnePendingTask() {
        crawlTaskWorker.recoverOldestPendingTask();
    }
}
