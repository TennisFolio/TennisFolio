package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMemberFilter;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMemberPage;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardMonthlyData;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.repository.ClubDashboardQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubDashboardQueryServiceTest {

    @Mock ClubAccessService clubAccessService;
    @Mock ClubDashboardQueryRepository dashboardQueryRepository;
    ClubDashboardQueryService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-11T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        service = new ClubDashboardQueryService(clubAccessService, dashboardQueryRepository, clock);
    }

    @Test
    void getDashboard_returnsSelectedMonthSummaryForAdmin() {
        ClubDashboardMonthlyData data = new ClubDashboardMonthlyData(
                10, 4, 7, 5, 7, new ClubDashboardMemberPage(List.of(), 0, 10, 10, 1), List.of()
        );
        when(clubAccessService.requireAdmin("club-public-id", 10L)).thenReturn(club());
        when(dashboardQueryRepository.findMonthlyDashboardData(
                eq(100L), eq(LocalDateTime.of(2026, 8, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 8, 31, 23, 59, 59, 999_999_999)),
                eq(ClubDashboardMemberFilter.PARTICIPATED), eq(1), eq(10)
        )).thenReturn(data);

        var response = service.getDashboard("club-public-id", 10L, YearMonth.of(2026, 8),
                ClubDashboardMemberFilter.PARTICIPATED, 1);

        assertThat(response.getPeriod().getYear()).isEqualTo(2026);
        assertThat(response.getPeriod().getMonth()).isEqualTo(8);
        assertThat(response.getMemberParticipation().getRate()).isEqualTo(70);
        assertThat(response.getMemberGuestRatio().getMemberRate()).isEqualTo(58);
        verify(clubAccessService).requireAdmin("club-public-id", 10L);
    }

    @Test
    void getDashboard_rejectsFutureMonth() {
        when(clubAccessService.requireAdmin("club-public-id", 10L)).thenReturn(club());

        assertThatThrownBy(() -> service.getDashboard("club-public-id", 10L, YearMonth.of(2026, 9),
                ClubDashboardMemberFilter.ALL, 0))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private static Club club() {
        Club club = new Club("Morning Tennis", "Indoor club", 10L);
        ReflectionTestUtils.setField(club, "id", 100L);
        return club;
    }
}
