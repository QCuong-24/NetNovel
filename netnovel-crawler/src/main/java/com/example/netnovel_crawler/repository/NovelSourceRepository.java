package com.example.netnovel_crawler.repository;

import com.example.netnovel_crawler.entity.NovelSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NovelSourceRepository extends JpaRepository<NovelSource, Long> {

    Optional<NovelSource> findBySourceNameAndSourceNovelUrl(String sourceName, String sourceNovelUrl);
}
