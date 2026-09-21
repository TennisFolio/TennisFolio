package com.tennisfolio.Tennisfolio.club.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ClubDashboardPeriodTest {

    @Test
    void period_exposesDateAndDateTimeBounds() {
        ClubDashboardPeriod period = new ClubDashboardPeriod(
                LocalDate.of(2026, 7, 13),
                LocalDate.of(2026, 8, 11)
        );

        assertThat(period.getFrom()).isEqualTo(LocalDate.of(2026, 7, 13));
        assertThat(period.getTo()).isEqualTo(LocalDate.of(2026, 8, 11));
        assertThat(period.getFromDateTime().toString()).isEqualTo("2026-07-13T00:00");
        assertThat(period.getToDateTime().toString()).isEqualTo("2026-08-11T23:59:59.999999999");
    }
}
