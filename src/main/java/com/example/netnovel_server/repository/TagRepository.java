package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByOrderByNameAsc();

    Optional<Tag> findByName(String name);

    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);
}
