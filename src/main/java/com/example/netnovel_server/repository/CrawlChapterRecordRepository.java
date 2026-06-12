package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.CrawlChapterRecord;
import com.example.netnovel_server.entity.CrawlChapterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CrawlChapterRecordRepository extends JpaRepository<CrawlChapterRecord, Long> {

    Optional<CrawlChapterRecord> findBySourceNameAndSourceChapterUrl(String sourceName, String sourceChapterUrl);

    boolean existsBySourceNameAndSourceChapterUrlAndStatus(
        String sourceName,
        String sourceChapterUrl,
        CrawlChapterStatus status
    );
}
