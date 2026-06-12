package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.NovelSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NovelSourceRepository extends JpaRepository<NovelSource, Long> {

    Optional<NovelSource> findBySourceNameAndSourceNovelUrl(String sourceName, String sourceNovelUrl);

    boolean existsBySourceNameAndSourceNovelUrl(String sourceName, String sourceNovelUrl);
}
