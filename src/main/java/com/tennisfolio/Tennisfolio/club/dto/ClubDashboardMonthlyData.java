package com.tennisfolio.Tennisfolio.club.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ClubDashboardMonthlyData {

    private final long activeMemberCount;
    private final long meetingCount;
    private final long memberAttendanceCount;
    private final long guestAttendanceCount;
    private final long participantCount;
    private final ClubDashboardMemberPage members;
    private final List<ClubDashboardMonthlyMeeting> meetings;
}
