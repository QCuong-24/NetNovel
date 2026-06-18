package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.NovelChapterInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NovelChapterInfoRepository extends JpaRepository<NovelChapterInfo, Long> {
}
