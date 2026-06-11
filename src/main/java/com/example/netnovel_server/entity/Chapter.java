package com.example.netnovel_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "chapters",
    uniqueConstraints = @UniqueConstraint(columnNames = {"novel_id", "chapter_number"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer chapterNumber;

    @Column(nullable = false)
    private LocalDateTime updateAt;

    @OneToOne(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ChapterContent content;

    @PrePersist
    @PreUpdate
    private void updateTimestamp() {
        updateAt = LocalDateTime.now();
    }

    public void setContent(String content) {
        if (this.content == null) {
            this.content = new ChapterContent();
            this.content.setChapter(this);
        }
        this.content.setContent(content);
    }
}
