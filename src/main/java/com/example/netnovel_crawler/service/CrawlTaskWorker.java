package com.example.netnovel_crawler.service;

import com.example.netnovel_crawler.adapter.CrawlerAdapterDispatcher;
import com.example.netnovel_crawler.dto.CrawlNovelRequestMessage;
import com.example.netnovel_crawler.entity.CrawlTask;
import com.example.netnovel_crawler.entity.CrawlTaskStatus;
import com.example.netnovel_crawler.repository.CrawlTaskRepository;
import com.example.netnovel_crawler.source.CrawlerSource;
import com.example.netnovel_crawler.source.SourceRegistry;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CrawlTaskWorker {

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
        CrawlTask task = markRunning(message);

        sourceRegistry.resolve(message.getUrl()).ifPresentOrElse(
            source -> crawlSupportedSource(task.getId(), source, message),
            () -> markFinished(
                task.getId(),
                CrawlTaskStatus.SKIPPED_UNSUPPORTED_SOURCE,
                "Unsupported crawl source: " + message.getUrl()
            )
        );
    }

    @Transactional
    protected CrawlTask markRunning(CrawlNovelRequestMessage message) {
        CrawlTask task = findTask(message.getTaskId());
        task.setStatus(CrawlTaskStatus.RUNNING);
        task.setStartedAt(LocalDateTime.now());
        task.setFinishedAt(null);
        task.setErrorMessage(null);
        return crawlTaskRepository.save(task);
    }

    private void crawlSupportedSource(Long taskId, CrawlerSource source, CrawlNovelRequestMessage message) {
        try {
            crawlerAdapterDispatcher.crawlNovel(source, message);
            markFinished(taskId, CrawlTaskStatus.SUCCESS, null);
        } catch (Exception exception) {
            markFinished(taskId, CrawlTaskStatus.FAILED, exception.getMessage());
        }
    }

    @Transactional
    protected void markFinished(Long taskId, CrawlTaskStatus status, String errorMessage) {
        CrawlTask task = findTask(taskId);
        task.setStatus(status);
        task.setFinishedAt(LocalDateTime.now());
        task.setErrorMessage(errorMessage);
        crawlTaskRepository.save(task);
    }

    private CrawlTask findTask(Long taskId) {
        return crawlTaskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Crawl task not found: " + taskId));
    }
}
