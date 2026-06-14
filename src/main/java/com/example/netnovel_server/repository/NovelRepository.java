package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {

    Page<Novel> findByStatus(Status status, Pageable pageable);

    Page<Novel> findByStatusOrderByUpdateAtDesc(Status status, Pageable pageable);

    Page<Novel> findAllByOrderByUpdateAtDesc(Pageable pageable);

    Page<Novel> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Novel> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);

    Page<Novel> findByUpdateAtBetweenOrderByUpdateAtDesc(
        LocalDateTime start,
        LocalDateTime end,
        Pageable pageable
    );

    List<Novel> findTop10ByOrderByViewsDesc();

    List<Novel> findTop10ByOrderByFollowsDesc();

    List<Novel> findTop10ByOrderByLikesDesc();

    List<Novel> findByGenresId(Long genreId);

    List<Novel> findByTagsId(Long tagId);
}
