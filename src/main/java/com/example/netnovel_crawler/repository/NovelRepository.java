package com.example.netnovel_crawler.repository;

import com.example.netnovel_crawler.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {
}
