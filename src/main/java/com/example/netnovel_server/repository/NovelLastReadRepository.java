package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.NovelLastRead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NovelLastReadRepository extends JpaRepository<NovelLastRead, Long> {

    Page<NovelLastRead> findByUserIdOrderByLastReadAtDesc(Long userId, Pageable pageable);

    Page<NovelLastRead> findByLastReadAtBetweenOrderByLastReadAtDesc(
        LocalDateTime start,
        LocalDateTime end,
        Pageable pageable
    );

    Page<NovelLastRead> findByUserIdAndLastReadAtBetweenOrderByLastReadAtDesc(
        Long userId,
        LocalDateTime start,
        LocalDateTime end,
        Pageable pageable
    );

    Optional<NovelLastRead> findByUserIdAndNovelId(Long userId, Long novelId);

    void deleteByUserIdAndNovelId(Long userId, Long novelId);
}
