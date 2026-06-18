package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(
    name = "novel_view_stats",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_novel_view_stats_novel_date",
        columnNames = {"novel_id", "view_date"}
    ),
    indexes = {
        @Index(name = "idx_novel_view_stats_novel", columnList = "novel_id"),
        @Index(name = "idx_novel_view_stats_view_date", columnList = "view_date"),
        @Index(name = "idx_novel_view_stats_novel_view_date", columnList = "novel_id, view_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelViewStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Novel novel;

    @Column(nullable = false)
    private LocalDate viewDate;

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;
}
