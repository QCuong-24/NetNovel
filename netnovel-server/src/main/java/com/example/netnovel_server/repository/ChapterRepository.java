package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    Page<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId, Pageable pageable);

    List<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId);

    List<Chapter> findTop3ByNovelIdOrderByChapterNumberAsc(Long novelId);

    Optional<Chapter> findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);

    Optional<Chapter> findTopByNovelIdOrderByChapterNumberDesc(Long novelId);

    long countByNovelId(Long novelId);

    long countByNovelIdAndChapterNumberLessThan(Long novelId, Integer chapterNumber);

    boolean existsByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);
}
