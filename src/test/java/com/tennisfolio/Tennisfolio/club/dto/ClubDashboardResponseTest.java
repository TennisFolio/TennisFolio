package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClubDashboardResponseTest {

    @Test
    void response_exposesDashboardSectionsAsTheApiContract() {
        ClubDashboardResponse response = new ClubDashboardResponse(
                new ClubDashboardPeriod(LocalDate.of(2026, 7, 13), LocalDate.of(2026, 8, 11)),
                42,
                8,
                new ClubDashboardMemberParticipation(31, 73, new ClubDashboardMemberPage(List.of(), 0, 10, 0, 0)),
                new ClubDashboardMemberGuestRatio(58, 12, 83, 17),
                List.of(new ClubDashboardMonthlyMeeting(
                        "meeting-public-id",
                        LocalDateTime.of(2026, 8, 10, 9, 0),
                        "Sunday doubles",
                        MeetingStatus.OPEN,
                        14,
                        2
                ))
        );

        assertThat(response.getPeriod().getFrom()).isEqualTo(LocalDate.of(2026, 7, 13));
        assertThat(response.getMemberParticipation().getRate()).isEqualTo(73);
        assertThat(response.getMemberGuestRatio().getGuestRate()).isEqualTo(17);
        assertThat(response.getMeetings().get(0).getGuestAttendanceCount()).isEqualTo(2);
    }
}
