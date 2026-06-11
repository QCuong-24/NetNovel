package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Bookmark> findByUserIdAndChapterId(Long userId, Long chapterId);

    boolean existsByUserIdAndChapterId(Long userId, Long chapterId);

    void deleteByUserIdAndChapterId(Long userId, Long chapterId);
}
