package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
    name = "novel_user_views",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_novel_user_views_user_novel",
        columnNames = {"user_id", "novel_id"}
    ),
    indexes = {
        @Index(name = "idx_novel_user_views_user", columnList = "user_id"),
        @Index(name = "idx_novel_user_views_novel", columnList = "novel_id"),
        @Index(name = "idx_novel_user_views_user_novel", columnList = "user_id, novel_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelUserView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;
}
