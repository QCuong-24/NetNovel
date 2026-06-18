package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    void deleteByEventAtBefore(LocalDateTime threshold);
}
