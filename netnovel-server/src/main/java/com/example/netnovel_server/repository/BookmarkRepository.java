package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Bookmark> findByUserIdAndNovelIsNotNullOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Bookmark> findByUserIdAndChapterIsNotNullOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Bookmark> findByIdAndUserId(Long id, Long userId);

    Optional<Bookmark> findByUserIdAndNovelId(Long userId, Long novelId);

    Optional<Bookmark> findByUserIdAndChapterId(Long userId, Long chapterId);

    boolean existsByUserIdAndNovelId(Long userId, Long novelId);

    boolean existsByUserIdAndChapterId(Long userId, Long chapterId);

    long countByChapterId(Long chapterId);

    void deleteByUserIdAndNovelId(Long userId, Long novelId);

    void deleteByUserIdAndChapterId(Long userId, Long chapterId);

    void deleteByNovelId(Long novelId);

    void deleteByChapterId(Long chapterId);

    void deleteByChapterNovelId(Long novelId);

    @Query("""
        select count(b)
        from Bookmark b
        where b.createdAt between :start and :end
        """)
    long countTotalBookmarksBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        select n as novel, count(b.id) as bookmarkCount
        from Bookmark b
        left join b.novel directNovel
        left join b.chapter chapter
        left join chapter.novel chapterNovel
        join Novel n on n.id = coalesce(directNovel.id, chapterNovel.id)
        where b.createdAt between :start and :end
        group by n
        order by count(b.id) desc
        """)
    Page<BookmarkNovelCount> findTopBookmarkedNovelsBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );
}
