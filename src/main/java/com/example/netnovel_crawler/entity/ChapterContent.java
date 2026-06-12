package com.example.netnovel_crawler.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chapter_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterContent {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
