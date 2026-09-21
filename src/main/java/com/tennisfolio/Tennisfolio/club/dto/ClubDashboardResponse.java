package com.tennisfolio.Tennisfolio.club.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ClubDashboardResponse {

    private final ClubDashboardPeriod period;
    private final long activeMemberCount;
    private final long meetingCount;
    private final ClubDashboardMemberParticipation memberParticipation;
    private final ClubDashboardMemberGuestRatio memberGuestRatio;
    private final List<ClubDashboardMonthlyMeeting> meetings;
}
