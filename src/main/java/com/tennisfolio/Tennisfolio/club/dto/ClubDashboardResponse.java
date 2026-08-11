package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ClubDashboardResponse {

    private final Period period;
    private final long activeMemberCount;
    private final long meetingCount;
    private final long cancelledMeetingCount;
    private final MemberParticipation memberParticipation;
    private final long guestAttendanceCount;
    private final double averageAttendancePerMeeting;
    private final MemberComposition memberComposition;
    private final List<RecentMeeting> recentMeetings;

    @Getter
    @RequiredArgsConstructor
    public static class Period {
        private final LocalDate from;
        private final LocalDate to;
    }

    @Getter
    @RequiredArgsConstructor
    public static class MemberParticipation {
        private final long participantCount;
        private final int rate;
        private final long attendanceCount;
        private final long inactiveParticipantCount;
    }

    @Getter
    @RequiredArgsConstructor
    public static class MemberComposition {
        private final List<GenderCount> genderCounts;
        private final List<SkillTierCount> skillTierCounts;
        private final long unclassifiedSkillMemberCount;
    }

    @Getter
    @RequiredArgsConstructor
    public static class GenderCount {
        private final Gender gender;
        private final long count;
    }

    @Getter
    @RequiredArgsConstructor
    public static class SkillTierCount {
        private final Long skillTierId;
        private final String name;
        private final int level;
        private final long count;
    }

    @Getter
    @RequiredArgsConstructor
    public static class RecentMeeting {
        private final String publicId;
        private final LocalDateTime startAt;
        private final String title;
        private final MeetingStatus status;
        private final long memberAttendanceCount;
        private final long guestAttendanceCount;
    }
}
