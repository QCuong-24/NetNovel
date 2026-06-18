package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_events",
    indexes = {
        @Index(name = "idx_user_events_user_event_at", columnList = "user_id, event_at"),
        @Index(name = "idx_user_events_novel_event_at", columnList = "novel_id, event_at"),
        @Index(name = "idx_user_events_type_event_at", columnList = "event_type, event_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Chapter chapter;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private UserEventType eventType;

    @Column(nullable = false)
    private LocalDateTime eventAt;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @PrePersist
    private void setEventAt() {
        if (eventAt == null) {
            eventAt = LocalDateTime.now();
        }
    }
}
