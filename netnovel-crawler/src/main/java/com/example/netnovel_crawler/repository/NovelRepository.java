package com.example.netnovel_crawler.repository;

import com.example.netnovel_crawler.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update Novel novel
        set novel.updateAt = :updatedAt
        where novel.id = :novelId
          and novel.updateAt < :updatedAt
        """)
    void advanceUpdateAt(@Param("novelId") Long novelId, @Param("updatedAt") LocalDateTime updatedAt);
}
