package com.tennisfolio.Tennisfolio.club.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ClubDashboardData {

    private final long activeMemberCount;
    private final List<ClubDashboardGenderCount> genderCounts;
    private final List<ClubDashboardSkillTierCount> skillTierCounts;
    private final long unclassifiedSkillMemberCount;
    private final long meetingCount;
    private final long cancelledMeetingCount;
    private final long memberAttendanceCount;
    private final long guestAttendanceCount;
    private final long participantCount;
    private final List<ClubDashboardRecentMeeting> recentMeetings;
}
