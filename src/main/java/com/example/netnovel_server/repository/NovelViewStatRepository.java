package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.NovelViewStat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

@Repository
public interface NovelViewStatRepository extends JpaRepository<NovelViewStat, Long> {

    void deleteByNovelId(Long novelId);

    @Modifying
    @Query(
        value = """
            insert into novel_view_stats (novel_id, view_date, view_count)
            values (:novelId, :viewDate, 1)
            on conflict (novel_id, view_date)
            do update set view_count = novel_view_stats.view_count + 1
            """,
        nativeQuery = true
    )
    void incrementViewCount(@Param("novelId") Long novelId, @Param("viewDate") LocalDate viewDate);

    @Query("""
        select coalesce(sum(v.viewCount), 0)
        from NovelViewStat v
        where v.viewDate between :start and :end
        """)
    long countTotalViewsBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
        select v.novel as novel, coalesce(sum(v.viewCount), 0) as viewCount
        from NovelViewStat v
        where v.viewDate between :start and :end
        group by v.novel
        order by coalesce(sum(v.viewCount), 0) desc
        """)
    Page<NovelViewCount> findTopViewedNovelsBetween(
        @Param("start") LocalDate start,
        @Param("end") LocalDate end,
        Pageable pageable
    );

    default long countDailyViews(LocalDate date) {
        return countTotalViewsBetween(date, date);
    }

    default long countMonthlyViews(YearMonth month) {
        return countTotalViewsBetween(month.atDay(1), month.atEndOfMonth());
    }

    default long countYearlyViews(Year year) {
        return countTotalViewsBetween(year.atDay(1), year.atMonth(12).atEndOfMonth());
    }

    default Page<NovelViewCount> findTopDailyViewedNovels(LocalDate date, Pageable pageable) {
        return findTopViewedNovelsBetween(date, date, pageable);
    }

    default Page<NovelViewCount> findTopMonthlyViewedNovels(YearMonth month, Pageable pageable) {
        return findTopViewedNovelsBetween(month.atDay(1), month.atEndOfMonth(), pageable);
    }

    default Page<NovelViewCount> findTopYearlyViewedNovels(Year year, Pageable pageable) {
        return findTopViewedNovelsBetween(year.atDay(1), year.atMonth(12).atEndOfMonth(), pageable);
    }
}
