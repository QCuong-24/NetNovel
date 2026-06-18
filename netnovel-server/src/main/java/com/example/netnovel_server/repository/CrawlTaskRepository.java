package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.CrawlTask;
import com.example.netnovel_server.entity.CrawlTaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlTaskRepository extends JpaRepository<CrawlTask, Long> {

    Page<CrawlTask> findAllByOrderByCreateAtDesc(Pageable pageable);

    Page<CrawlTask> findByStatusOrderByCreateAtDesc(CrawlTaskStatus status, Pageable pageable);

    Page<CrawlTask> findByRequestedByIdOrderByCreateAtDesc(Long requestedByUserId, Pageable pageable);

    Page<CrawlTask> findByStatusAndRequestedByIdOrderByCreateAtDesc(
        CrawlTaskStatus status,
        Long requestedByUserId,
        Pageable pageable
    );
}
