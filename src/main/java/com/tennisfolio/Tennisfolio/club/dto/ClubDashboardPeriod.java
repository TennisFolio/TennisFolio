package com.tennisfolio.Tennisfolio.club.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@RequiredArgsConstructor
public class ClubDashboardPeriod {

    private final LocalDate from;
    private final LocalDate to;

    @JsonIgnore
    public LocalDateTime getFromDateTime() {
        return from.atStartOfDay();
    }

    @JsonIgnore
    public LocalDateTime getToDateTime() {
        return to.atTime(LocalTime.MAX);
    }
}
