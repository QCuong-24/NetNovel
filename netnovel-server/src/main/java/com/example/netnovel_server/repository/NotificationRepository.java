package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    void deleteByUserId(Long userId);
}
