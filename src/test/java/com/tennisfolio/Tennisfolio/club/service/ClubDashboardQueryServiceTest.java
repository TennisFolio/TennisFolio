package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardData;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardGenderCount;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardRecentMeeting;
import com.tennisfolio.Tennisfolio.club.dto.ClubDashboardSkillTierCount;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.repository.ClubDashboardQueryRepository;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubDashboardQueryServiceTest {

    @Mock
    ClubAccessService clubAccessService;

    @Mock
    ClubDashboardQueryRepository dashboardQueryRepository;

    ClubDashboardQueryService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-11T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        service = new ClubDashboardQueryService(clubAccessService, dashboardQueryRepository, clock);
    }

    @Test
    void getDashboard_requiresAdminAndAssemblesThirtyDaySummary() {
        Club club = club();
        ClubDashboardData dashboardData = new ClubDashboardData(
                10,
                List.of(new ClubDashboardGenderCount(Gender.MALE, 6)),
                List.of(new ClubDashboardSkillTierCount(1L, "Intermediate", 2, 7)),
                3,
                4,
                1,
                7,
                5,
                7,
                List.of(new ClubDashboardRecentMeeting(
                        "meeting-public-id",
                        LocalDateTime.of(2026, 8, 10, 10, 0),
                        "Sunday doubles",
                        MeetingStatus.OPEN,
                        4,
                        2
                ))
        );
        when(clubAccessService.requireAdmin("club-public-id", 10L)).thenReturn(club);
        when(dashboardQueryRepository.findDashboardData(
                eq(100L),
                eq(LocalDateTime.of(2026, 7, 13, 0, 0)),
                eq(LocalDateTime.of(2026, 8, 11, 23, 59, 59, 999_999_999))
        )).thenReturn(dashboardData);

        ClubDashboardResponse response = service.getDashboard("club-public-id", 10L);

        verify(clubAccessService).requireAdmin("club-public-id", 10L);
        assertThat(response.getPeriod().getFrom().toString()).isEqualTo("2026-07-13");
        assertThat(response.getPeriod().getTo().toString()).isEqualTo("2026-08-11");
        assertThat(response.getMemberParticipation().getRate()).isEqualTo(70);
        assertThat(response.getMemberParticipation().getInactiveParticipantCount()).isEqualTo(3);
        assertThat(response.getAverageAttendancePerMeeting()).isEqualTo(3.0);
        assertThat(response.getGuestAttendanceCount()).isEqualTo(5);
        assertThat(response.getRecentMeetings().get(0).getMemberAttendanceCount()).isEqualTo(4);
    }

    @Test
    void getDashboard_returnsZeroRatesWhenThereAreNoActiveMembersOrMeetings() {
        Club club = club();
        ClubDashboardData dashboardData = new ClubDashboardData(
                0, List.of(), List.of(), 0, 0, 0, 0, 0, 0, List.of()
        );
        when(clubAccessService.requireAdmin("club-public-id", 10L)).thenReturn(club);
        when(dashboardQueryRepository.findDashboardData(
                eq(100L),
                eq(LocalDateTime.of(2026, 7, 13, 0, 0)),
                eq(LocalDateTime.of(2026, 8, 11, 23, 59, 59, 999_999_999))
        )).thenReturn(dashboardData);

        ClubDashboardResponse response = service.getDashboard("club-public-id", 10L);

        assertThat(response.getMemberParticipation().getRate()).isZero();
        assertThat(response.getMemberParticipation().getInactiveParticipantCount()).isZero();
        assertThat(response.getAverageAttendancePerMeeting()).isZero();
    }

    private static Club club() {
        Club club = new Club("Morning Tennis", "Indoor club", 10L);
        ReflectionTestUtils.setField(club, "id", 100L);
        return club;
    }
}
