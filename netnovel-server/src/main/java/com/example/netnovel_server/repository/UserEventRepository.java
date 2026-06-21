package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    void deleteByEventAtBefore(LocalDateTime threshold);

    long countByEventAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("""
        select e.eventType as eventType, count(e) as eventCount
        from UserEvent e
        where e.eventAt between :from and :to
        group by e.eventType
        """)
    List<UserEventTypeCountProjection> countEventsByType(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query("""
        select count(distinct e.user.id)
        from UserEvent e
        where e.novel is not null and e.eventAt between :from and :to
        """)
    long countActiveUsersWithNovelInteractions(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query("""
        select count(distinct e.novel.id)
        from UserEvent e
        where e.novel is not null and e.eventAt between :from and :to
        """)
    long countInteractedNovels(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query("""
        select e.user.id as entityId, count(distinct e.novel.id) as distinctCount
        from UserEvent e
        where e.novel is not null and e.eventAt between :from and :to
        group by e.user.id
        """)
    List<UserEventInteractionCountProjection> countDistinctNovelsByUser(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query("""
        select e.novel.id as entityId, count(distinct e.user.id) as distinctCount
        from UserEvent e
        where e.novel is not null and e.eventAt between :from and :to
        group by e.novel.id
        """)
    List<UserEventInteractionCountProjection> countDistinctUsersByNovel(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}
