package com.example.netnovel_crawler.repository;

import com.example.netnovel_crawler.entity.NovelChapterInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NovelChapterInfoRepository extends JpaRepository<NovelChapterInfo, Long> {
}
