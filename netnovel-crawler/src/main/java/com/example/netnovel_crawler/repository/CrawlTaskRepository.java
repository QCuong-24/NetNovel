package com.example.netnovel_crawler.repository;

import com.example.netnovel_crawler.entity.CrawlTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlTaskRepository extends JpaRepository<CrawlTask, Long> {
}
