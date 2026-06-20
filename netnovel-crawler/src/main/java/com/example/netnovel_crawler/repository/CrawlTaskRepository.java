package com.example.netnovel_crawler.repository;

import com.example.netnovel_crawler.entity.CrawlTask;
import com.example.netnovel_crawler.entity.CrawlTaskStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CrawlTaskRepository extends JpaRepository<CrawlTask, Long> {

    Optional<CrawlTask> findFirstByStatusOrderByCreateAtAsc(CrawlTaskStatus status);

    @Modifying
    @Transactional
    @Query("""
        update CrawlTask task
        set task.status = :runningStatus,
            task.startedAt = :startedAt,
            task.finishedAt = null,
            task.errorMessage = null
        where task.id = :taskId
          and task.status = :pendingStatus
        """)
    int claimPendingTask(
        @Param("taskId") Long taskId,
        @Param("pendingStatus") CrawlTaskStatus pendingStatus,
        @Param("runningStatus") CrawlTaskStatus runningStatus,
        @Param("startedAt") LocalDateTime startedAt
    );
}
