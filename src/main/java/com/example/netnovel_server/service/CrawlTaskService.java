package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.CrawlNovelRequestMessage;
import com.example.netnovel_server.dto.CrawlTaskCreateDTO;
import com.example.netnovel_server.dto.CrawlTaskDTO;
import com.example.netnovel_server.entity.CrawlTask;
import com.example.netnovel_server.entity.CrawlTaskStatus;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.CrawlTaskMapper;
import com.example.netnovel_server.repository.CrawlTaskRepository;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrawlTaskService {

    private final CrawlTaskRepository crawlTaskRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final String crawlExchangeName;
    private final String crawlNovelRequestRoutingKey;

    public CrawlTaskService(
        CrawlTaskRepository crawlTaskRepository,
        UserRepository userRepository,
        RabbitTemplate rabbitTemplate,
        @Value("${app.crawl.rabbit.exchange:netnovel.crawl}") String crawlExchangeName,
        @Value("${app.crawl.rabbit.novel-request-routing-key:crawl.novel.request}") String crawlNovelRequestRoutingKey
    ) {
        this.crawlTaskRepository = crawlTaskRepository;
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.crawlExchangeName = crawlExchangeName;
        this.crawlNovelRequestRoutingKey = crawlNovelRequestRoutingKey;
    }

    @Transactional
    public CrawlTaskDTO createNovelCrawlTask(CrawlTaskCreateDTO request) {
        String url = validateUrl(request == null ? null : request.getUrl());
        User requestedBy = SecurityUtils.getCurrentUserId()
            .flatMap(userRepository::findById)
            .orElse(null);

        CrawlTask task = crawlTaskRepository.save(CrawlTask.builder()
            .url(url)
            .requestedBy(requestedBy)
            .status(CrawlTaskStatus.PENDING)
            .build());

        try {
            rabbitTemplate.convertAndSend(
                crawlExchangeName,
                crawlNovelRequestRoutingKey,
                CrawlNovelRequestMessage.builder()
                    .taskId(task.getId())
                    .url(task.getUrl())
                    .requestedByUserId(requestedBy == null ? null : requestedBy.getId())
                    .build()
            );
        } catch (AmqpException exception) {
            task.setStatus(CrawlTaskStatus.FAILED);
            task.setErrorMessage("Failed to publish crawl task: " + exception.getMessage());
        }

        return CrawlTaskMapper.toDTO(crawlTaskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public CrawlTaskDTO getTask(Long taskId) {
        return CrawlTaskMapper.toDTO(findTask(taskId));
    }

    @Transactional(readOnly = true)
    public Page<CrawlTaskDTO> getTasks(CrawlTaskStatus status, Pageable pageable) {
        Page<CrawlTask> tasks = status == null
            ? crawlTaskRepository.findAll(pageable)
            : crawlTaskRepository.findByStatusOrderByCreateAtDesc(status, pageable);
        return tasks.map(CrawlTaskMapper::toDTO);
    }

    private CrawlTask findTask(Long taskId) {
        return crawlTaskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Crawl task not found"));
    }

    private String validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BadRequestException("Crawl URL is required");
        }
        String trimmedUrl = url.trim();
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            throw new BadRequestException("Crawl URL must start with http:// or https://");
        }
        return trimmedUrl;
    }
}
