package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead, Pageable pageable);

    @Query("""
        select n
        from Notification n
        where n.user.id = :userId
          and (:isRead is null or n.isRead = :isRead)
          and (:type is null or n.type = :type)
        order by n.createdAt desc
        """)
    Page<Notification> findMyNotifications(
        @Param("userId") Long userId,
        @Param("isRead") Boolean isRead,
        @Param("type") String type,
        Pageable pageable
    );

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    void deleteByUserId(Long userId);
}
