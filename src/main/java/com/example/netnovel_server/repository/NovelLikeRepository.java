package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.NovelLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NovelLikeRepository extends JpaRepository<NovelLike, Long> {

    Page<NovelLike> findByUserIdOrderByLikedAtDesc(Long userId, Pageable pageable);

    Page<NovelLike> findByNovelIdOrderByLikedAtDesc(Long novelId, Pageable pageable);

    Optional<NovelLike> findByUserIdAndNovelId(Long userId, Long novelId);

    boolean existsByUserIdAndNovelId(Long userId, Long novelId);

    void deleteByUserIdAndNovelId(Long userId, Long novelId);
}
