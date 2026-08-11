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
                new ClubDashboardResponse.Period(LocalDate.of(2026, 7, 13), LocalDate.of(2026, 8, 11)),
                42,
                8,
                1,
                new ClubDashboardResponse.MemberParticipation(31, 73, 58, 11),
                12,
                8.8,
                new ClubDashboardResponse.MemberComposition(
                        List.of(new ClubDashboardResponse.GenderCount(Gender.MALE, 24)),
                        List.of(new ClubDashboardResponse.SkillTierCount(1L, "Intermediate B", 2, 24)),
                        3
                ),
                List.of(new ClubDashboardResponse.RecentMeeting(
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
        assertThat(response.getMemberComposition().getSkillTierCounts().get(0).getName()).isEqualTo("Intermediate B");
        assertThat(response.getRecentMeetings().get(0).getGuestAttendanceCount()).isEqualTo(2);
    }
}
