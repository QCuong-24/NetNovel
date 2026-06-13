package com.example.netnovel_crawler.repository;

import com.example.netnovel_crawler.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    Optional<Chapter> findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);

    Optional<Chapter> findTopByNovelIdOrderByChapterNumberDesc(Long novelId);

    long countByNovelId(Long novelId);
}
