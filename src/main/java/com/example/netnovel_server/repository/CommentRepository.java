package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByNovelIdAndParentCommentIsNullOrderByLastActivityAtDesc(Long novelId, Pageable pageable);

    Page<Comment> findByChapterIdAndParentCommentIsNullOrderByLastActivityAtDesc(Long chapterId, Pageable pageable);

    Page<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId, Pageable pageable);

    Page<Comment> findByRootCommentIdOrderByCreatedAtAsc(Long rootCommentId, Pageable pageable);

    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
