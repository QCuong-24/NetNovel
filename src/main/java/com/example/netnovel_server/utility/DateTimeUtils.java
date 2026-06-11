package com.example.netnovel_server.utility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    public static LocalDateTime startOfMonth(YearMonth month) {
        return month.atDay(1).atStartOfDay();
    }

    public static LocalDateTime endOfMonth(YearMonth month) {
        return month.atEndOfMonth().atTime(LocalTime.MAX);
    }

    public static LocalDateTime startOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
    }

    public static LocalDateTime endOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);
    }

    public static LocalDateTime startOfYear(Year year) {
        return year.atDay(1).atStartOfDay();
    }

    public static LocalDateTime endOfYear(Year year) {
        return year.atMonth(12).atEndOfMonth().atTime(LocalTime.MAX);
    }
}
