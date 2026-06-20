package com.example.netnovel_crawler.service;

import com.example.netnovel_crawler.adapter.CrawlerAdapterDispatcher;
import com.example.netnovel_crawler.dto.CrawlNovelRequestMessage;
import com.example.netnovel_crawler.entity.CrawlTask;
import com.example.netnovel_crawler.entity.CrawlTaskStatus;
import com.example.netnovel_crawler.repository.CrawlTaskRepository;
import com.example.netnovel_crawler.source.CrawlerSource;
import com.example.netnovel_crawler.source.SourceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CrawlTaskWorker {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskWorker.class);

    private final CrawlTaskRepository crawlTaskRepository;
    private final SourceRegistry sourceRegistry;
    private final CrawlerAdapterDispatcher crawlerAdapterDispatcher;

    public CrawlTaskWorker(
        CrawlTaskRepository crawlTaskRepository,
        SourceRegistry sourceRegistry,
        CrawlerAdapterDispatcher crawlerAdapterDispatcher
    ) {
        this.crawlTaskRepository = crawlTaskRepository;
        this.sourceRegistry = sourceRegistry;
        this.crawlerAdapterDispatcher = crawlerAdapterDispatcher;
    }

    @RabbitListener(queues = "${app.crawl.rabbit.novel-request-queue:crawl.novel.request}")
    public void handleNovelCrawlRequest(CrawlNovelRequestMessage message) {
        log.info("Received crawl task message. taskId={}, url={}", message.getTaskId(), message.getUrl());
        if (!claimPendingTask(message.getTaskId())) {
            log.info("Ignoring crawl task message because the task is no longer pending. taskId={}", message.getTaskId());
            return;
        }

        processClaimedTask(message.getTaskId(), message.getUrl(), message);
    }

    public void recoverOldestPendingTask() {
        crawlTaskRepository.findFirstByStatusOrderByCreateAtAsc(CrawlTaskStatus.PENDING)
            .ifPresent(task -> {
                if (!claimPendingTask(task.getId())) {
                    return;
                }

                log.info("Recovering oldest pending crawl task. taskId={}, url={}", task.getId(), task.getUrl());
                processClaimedTask(
                    task.getId(),
                    task.getUrl(),
                    CrawlNovelRequestMessage.builder()
                        .taskId(task.getId())
                        .url(task.getUrl())
                        .build()
                );
            });
    }

    private boolean claimPendingTask(Long taskId) {
        int claimed = crawlTaskRepository.claimPendingTask(
            taskId,
            CrawlTaskStatus.PENDING,
            CrawlTaskStatus.RUNNING,
            LocalDateTime.now()
        );
        if (claimed == 1) {
            log.info("Marked crawl task RUNNING. taskId={}", taskId);
            return true;
        }
        return false;
    }

    private void processClaimedTask(Long taskId, String url, CrawlNovelRequestMessage message) {
        sourceRegistry.resolve(url).ifPresentOrElse(
            source -> crawlSupportedSource(taskId, source, message),
            () -> markFinished(
                taskId,
                CrawlTaskStatus.SKIPPED_UNSUPPORTED_SOURCE,
                "Unsupported crawl source: " + url
            )
        );
    }

    private void crawlSupportedSource(Long taskId, CrawlerSource source, CrawlNovelRequestMessage message) {
        try {
            log.info(
                "Resolved crawl source. taskId={}, source={}, domain={}, engine={}",
                taskId,
                source.name(),
                source.domain(),
                source.engine()
            );
            crawlerAdapterDispatcher.crawlNovel(source, message);
            markFinished(taskId, CrawlTaskStatus.SUCCESS, null);
        } catch (Exception exception) {
            log.error("Crawl task failed. taskId={}, url={}", taskId, message.getUrl(), exception);
            markFinished(taskId, CrawlTaskStatus.FAILED, exception.getMessage());
        }
    }

    private void markFinished(Long taskId, CrawlTaskStatus status, String errorMessage) {
        CrawlTask task = findTask(taskId);
        task.setStatus(status);
        task.setFinishedAt(LocalDateTime.now());
        task.setErrorMessage(errorMessage);
        log.info("Marked crawl task finished. taskId={}, status={}, error={}", taskId, status, errorMessage);
        crawlTaskRepository.save(task);
    }

    private CrawlTask findTask(Long taskId) {
        return crawlTaskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Crawl task not found: " + taskId));
    }
}
