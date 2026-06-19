package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByNovelIdAndParentCommentIsNullOrderByLastActivityAtDesc(Long novelId, Pageable pageable);

    Page<Comment> findByChapterIdAndParentCommentIsNullOrderByLastActivityAtDesc(Long chapterId, Pageable pageable);

    Page<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId, Pageable pageable);

    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    Page<Comment> findByRootCommentIdOrderByCreatedAtAsc(Long rootCommentId, Pageable pageable);

    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Comment> findByChapterId(Long chapterId);

    @Query("""
        select distinct c
        from Comment c
        left join c.chapter ch
        left join ch.novel chapterNovel
        where c.novel.id = :novelId or chapterNovel.id = :novelId
        """)
    List<Comment> findAllByNovelOrChapterNovel(@Param("novelId") Long novelId);

    @Query("""
        select count(c)
        from Comment c
        where c.createdAt between :start and :end
        """)
    long countTotalCommentsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        select n as novel, count(c.id) as commentCount
        from Comment c
        left join c.novel directNovel
        left join c.chapter chapter
        left join chapter.novel chapterNovel
        join Novel n on n.id = coalesce(directNovel.id, chapterNovel.id)
        where c.createdAt between :start and :end
        group by n
        order by count(c.id) desc
        """)
    Page<CommentNovelCount> findTopCommentedNovelsBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );
}
