package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "novel_chapter_infos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelChapterInfo {

    @Id
    @Column(name = "novel_id")
    private Long novelId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "novel_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Novel novel;

    @Column(nullable = false)
    @Builder.Default
    private Integer chapterCount = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "latest_chapter_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Chapter latestChapter;

    private Integer latestChapterNumber;

    private String latestChapterTitle;

    private LocalDateTime latestChapterUpdatedAt;

    @Column(nullable = false)
    private LocalDateTime updateAt;

    @PrePersist
    @PreUpdate
    private void setUpdateAt() {
        updateAt = LocalDateTime.now();
    }
}
