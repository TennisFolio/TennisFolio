package com.tennisfolio.Tennisfolio.club.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

@Getter
@RequiredArgsConstructor
public class ClubDashboardPeriod {

    private final int year;
    private final int month;
    private final LocalDate from;
    private final LocalDate to;

    public ClubDashboardPeriod(YearMonth yearMonth) {
        this(yearMonth.getYear(), yearMonth.getMonthValue(), yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    public ClubDashboardPeriod(LocalDate from, LocalDate to) {
        this(from.getYear(), from.getMonthValue(), from, to);
    }

    @JsonIgnore
    public LocalDateTime getFromDateTime() {
        return from.atStartOfDay();
    }

    @JsonIgnore
    public LocalDateTime getToDateTime() {
        return to.atTime(LocalTime.MAX);
    }
}
