package com.example.netnovel_server.utility;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateTimeUtilsTest {

    // Date boundary helpers used by ranking/statistic queries.

    @Test
    void startAndEndOfDayUseFullDayBounds() {
        LocalDate date = LocalDate.of(2026, 7, 5);

        assertEquals(LocalDateTime.of(2026, 7, 5, 0, 0), DateTimeUtils.startOfDay(date));
        assertEquals(LocalDateTime.of(date, LocalTime.MAX), DateTimeUtils.endOfDay(date));
    }

    @Test
    void startAndEndOfMonthUseCalendarMonthBounds() {
        YearMonth month = YearMonth.of(2024, 2);

        assertEquals(LocalDateTime.of(2024, 2, 1, 0, 0), DateTimeUtils.startOfMonth(month));
        assertEquals(LocalDateTime.of(LocalDate.of(2024, 2, 29), LocalTime.MAX), DateTimeUtils.endOfMonth(month));
    }

    @Test
    void startAndEndOfWeekUseMondayAndSundayBounds() {
        LocalDate thursday = LocalDate.of(2026, 7, 9);

        assertEquals(LocalDateTime.of(2026, 7, 6, 0, 0), DateTimeUtils.startOfWeek(thursday));
        assertEquals(LocalDateTime.of(LocalDate.of(2026, 7, 12), LocalTime.MAX), DateTimeUtils.endOfWeek(thursday));
    }

    @Test
    void startAndEndOfYearUseCalendarYearBounds() {
        Year year = Year.of(2026);

        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), DateTimeUtils.startOfYear(year));
        assertEquals(LocalDateTime.of(LocalDate.of(2026, 12, 31), LocalTime.MAX), DateTimeUtils.endOfYear(year));
    }
}
