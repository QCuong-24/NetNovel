package com.example.netnovel_server.recommendation.repository;

import com.example.netnovel_server.recommendation.entity.UserNovelInteraction;
import com.example.netnovel_server.recommendation.entity.UserNovelInteractionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNovelInteractionRepository extends JpaRepository<UserNovelInteraction, UserNovelInteractionId> {

    Page<UserNovelInteraction> findAllByOrderByInteractionScoreDesc(Pageable pageable);

    Page<UserNovelInteraction> findByUserIdOrderByInteractionScoreDesc(Long userId, Pageable pageable);

    List<UserNovelInteraction> findByUserIdOrderByInteractionScoreDesc(Long userId);
}
