package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_user_created_at", columnList = "user_id, created_at"),
        @Index(name = "idx_notifications_user_is_read", columnList = "user_id, is_read")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // e.g. NEW_CHAPTER, FOLLOWER_UPDATE, SYSTEM
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    // URL to the related novel, chapter, or app page.
    private String link;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prepareForPersist() {
        if (isRead == null) {
            isRead = false;
        }
        createdAt = LocalDateTime.now();
    }
}
