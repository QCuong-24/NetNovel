package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.CrawlChapterRecordDTO;
import com.example.netnovel_server.dto.CrawlTaskCreateDTO;
import com.example.netnovel_server.dto.CrawlTaskDTO;
import com.example.netnovel_server.entity.CrawlChapterRecord;
import com.example.netnovel_server.entity.CrawlChapterStatus;
import com.example.netnovel_server.entity.CrawlTask;
import com.example.netnovel_server.entity.CrawlTaskStatus;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.event.CrawlTaskCreatedEvent;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.CrawlChapterRecordMapper;
import com.example.netnovel_server.mapper.CrawlTaskMapper;
import com.example.netnovel_server.repository.CrawlChapterRecordRepository;
import com.example.netnovel_server.repository.CrawlTaskRepository;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrawlTaskService {

    private final CrawlTaskRepository crawlTaskRepository;
    private final CrawlChapterRecordRepository crawlChapterRecordRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CrawlTaskService(
        CrawlTaskRepository crawlTaskRepository,
        CrawlChapterRecordRepository crawlChapterRecordRepository,
        UserRepository userRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.crawlTaskRepository = crawlTaskRepository;
        this.crawlChapterRecordRepository = crawlChapterRecordRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
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

        eventPublisher.publishEvent(new CrawlTaskCreatedEvent(
            task.getId(),
            task.getUrl(),
            requestedBy == null ? null : requestedBy.getId()
        ));

        return CrawlTaskMapper.toDTO(task);
    }

    @Transactional(readOnly = true)
    public CrawlTaskDTO getTask(Long taskId) {
        return CrawlTaskMapper.toDTO(findTask(taskId));
    }

    @Transactional(readOnly = true)
    public Page<CrawlTaskDTO> getTasks(CrawlTaskStatus status, boolean personal, Pageable pageable) {
        Long currentUserId = personal ? SecurityUtils.getCurrentUserIdOrThrow() : null;
        Page<CrawlTask> tasks;

        if (status != null && personal) {
            tasks = crawlTaskRepository.findByStatusAndRequestedByIdOrderByCreateAtDesc(status, currentUserId, pageable);
        } else if (status != null) {
            tasks = crawlTaskRepository.findByStatusOrderByCreateAtDesc(status, pageable);
        } else if (personal) {
            tasks = crawlTaskRepository.findByRequestedByIdOrderByCreateAtDesc(currentUserId, pageable);
        } else {
            tasks = crawlTaskRepository.findAllByOrderByCreateAtDesc(pageable);
        }

        return tasks.map(CrawlTaskMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<CrawlChapterRecordDTO> getCrawlChapterRecords(
        CrawlChapterStatus status,
        Long novelId,
        LocalDateTime start,
        LocalDateTime end,
        Pageable pageable
    ) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Start time must be before end time");
        }

        return crawlChapterRecordRepository
            .findAll(chapterRecordSpecification(status, novelId, start, end), pageable)
            .map(CrawlChapterRecordMapper::toDTO);
    }

    @Transactional
    public void deleteCrawlChapterRecord(Long recordId) {
        CrawlChapterRecord record = crawlChapterRecordRepository.findById(recordId)
            .orElseThrow(() -> new ResourceNotFoundException("Crawl chapter record not found"));

        crawlChapterRecordRepository.delete(record);
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

    private Specification<CrawlChapterRecord> chapterRecordSpecification(
        CrawlChapterStatus status,
        Long novelId,
        LocalDateTime start,
        LocalDateTime end
    ) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (novelId != null) {
                predicates.add(criteriaBuilder.equal(root.get("novel").get("id"), novelId));
            }
            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("crawledAt"), start));
            }
            if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("crawledAt"), end));
            }

            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
