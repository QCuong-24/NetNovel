package com.example.netnovel_server.audio.repository;

import com.example.netnovel_server.audio.entity.AudioUsageCounter;
import com.example.netnovel_server.audio.entity.AudioUsagePeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AudioUsageCounterRepository extends JpaRepository<AudioUsageCounter, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AudioUsageCounter> findByUser_IdAndPeriodTypeAndPeriodStart(
        Long userId,
        AudioUsagePeriodType periodType,
        LocalDate periodStart
    );
}
