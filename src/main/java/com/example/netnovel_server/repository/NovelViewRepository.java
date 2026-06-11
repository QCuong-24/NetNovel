package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.NovelView;
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

@Repository
public interface NovelViewRepository extends JpaRepository<NovelView, Long> {

    Page<NovelView> findByNovelIdOrderByViewedAtDesc(Long novelId, Pageable pageable);

    Page<NovelView> findByUserIdOrderByViewedAtDesc(Long userId, Pageable pageable);

    void deleteByNovelId(Long novelId);

    @Query("""
        select count(v)
        from NovelView v
        where v.viewedAt between :start and :end
        """)
    long countTotalViewsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        select v.novel as novel, count(v.id) as viewCount
        from NovelView v
        where v.viewedAt between :start and :end
        group by v.novel
        order by count(v.id) desc
        """)
    Page<NovelViewCount> findTopViewedNovelsBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );

    default long countDailyViews(LocalDate date) {
        return countTotalViewsBetween(date.atStartOfDay(), date.atTime(LocalTime.MAX));
    }

    default long countMonthlyViews(YearMonth month) {
        return countTotalViewsBetween(month.atDay(1).atStartOfDay(), month.atEndOfMonth().atTime(LocalTime.MAX));
    }

    default long countYearlyViews(Year year) {
        LocalDate firstDay = year.atDay(1);
        LocalDate lastDay = year.atMonth(12).atEndOfMonth();
        return countTotalViewsBetween(firstDay.atStartOfDay(), lastDay.atTime(LocalTime.MAX));
    }

    default Page<NovelViewCount> findTopDailyViewedNovels(LocalDate date, Pageable pageable) {
        return findTopViewedNovelsBetween(date.atStartOfDay(), date.atTime(LocalTime.MAX), pageable);
    }

    default Page<NovelViewCount> findTopMonthlyViewedNovels(YearMonth month, Pageable pageable) {
        return findTopViewedNovelsBetween(
            month.atDay(1).atStartOfDay(),
            month.atEndOfMonth().atTime(LocalTime.MAX),
            pageable
        );
    }

    default Page<NovelViewCount> findTopYearlyViewedNovels(Year year, Pageable pageable) {
        LocalDate firstDay = year.atDay(1);
        LocalDate lastDay = year.atMonth(12).atEndOfMonth();
        return findTopViewedNovelsBetween(firstDay.atStartOfDay(), lastDay.atTime(LocalTime.MAX), pageable);
    }
}
