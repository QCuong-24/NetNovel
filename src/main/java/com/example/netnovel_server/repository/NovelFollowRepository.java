package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.NovelFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface NovelFollowRepository extends JpaRepository<NovelFollow, Long> {

    Page<NovelFollow> findByUserIdOrderByFollowedAtDesc(Long userId, Pageable pageable);

    Page<NovelFollow> findByNovelIdOrderByFollowedAtDesc(Long novelId, Pageable pageable);

    List<NovelFollow> findByNovelId(Long novelId);

    Optional<NovelFollow> findByUserIdAndNovelId(Long userId, Long novelId);

    boolean existsByUserIdAndNovelId(Long userId, Long novelId);

    void deleteByUserIdAndNovelId(Long userId, Long novelId);

    void deleteByNovelId(Long novelId);

    @Query("""
        select count(f)
        from NovelFollow f
        where f.followedAt between :start and :end
        """)
    long countTotalFollowsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        select f.novel as novel, count(f.id) as followCount
        from NovelFollow f
        where f.followedAt between :start and :end
        group by f.novel
        order by count(f.id) desc
        """)
    Page<NovelFollowCount> findTopFollowedNovelsBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );

    default long countDailyFollows(LocalDate date) {
        return countTotalFollowsBetween(date.atStartOfDay(), date.atTime(LocalTime.MAX));
    }

    default long countMonthlyFollows(YearMonth month) {
        return countTotalFollowsBetween(month.atDay(1).atStartOfDay(), month.atEndOfMonth().atTime(LocalTime.MAX));
    }

    default long countYearlyFollows(Year year) {
        LocalDate firstDay = year.atDay(1);
        LocalDate lastDay = year.atMonth(12).atEndOfMonth();
        return countTotalFollowsBetween(firstDay.atStartOfDay(), lastDay.atTime(LocalTime.MAX));
    }

    default Page<NovelFollowCount> findTopDailyFollowedNovels(LocalDate date, Pageable pageable) {
        return findTopFollowedNovelsBetween(date.atStartOfDay(), date.atTime(LocalTime.MAX), pageable);
    }

    default Page<NovelFollowCount> findTopMonthlyFollowedNovels(YearMonth month, Pageable pageable) {
        return findTopFollowedNovelsBetween(
            month.atDay(1).atStartOfDay(),
            month.atEndOfMonth().atTime(LocalTime.MAX),
            pageable
        );
    }

    default Page<NovelFollowCount> findTopYearlyFollowedNovels(Year year, Pageable pageable) {
        LocalDate firstDay = year.atDay(1);
        LocalDate lastDay = year.atMonth(12).atEndOfMonth();
        return findTopFollowedNovelsBetween(firstDay.atStartOfDay(), lastDay.atTime(LocalTime.MAX), pageable);
    }
}
