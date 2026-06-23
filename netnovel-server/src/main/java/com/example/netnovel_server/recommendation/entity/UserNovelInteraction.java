package com.example.netnovel_server.recommendation.entity;

import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_novel_interactions",
    indexes = {
        @Index(name = "idx_user_novel_interactions_novel", columnList = "novel_id"),
        @Index(name = "idx_user_novel_interactions_last_interacted", columnList = "last_interacted_at"),
        @Index(name = "idx_user_novel_interactions_score", columnList = "interaction_score")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNovelInteraction {

    @EmbeddedId
    private UserNovelInteractionId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("novelId")
    @JoinColumn(name = "novel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Novel novel;

    @Column(nullable = false)
    @Builder.Default
    private Long viewNovelCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long viewChapterCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long commentCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long replyCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Boolean followed = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean liked = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean bookmarked = false;

    @Column(nullable = false)
    @Builder.Default
    private Double interactionScore = 0.0;

    @Column(nullable = false)
    private LocalDateTime firstInteractedAt;

    @Column(nullable = false)
    private LocalDateTime lastInteractedAt;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;
}
