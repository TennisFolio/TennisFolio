package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ClubDashboardMonthlyMeeting {

    private final String publicId;
    private final LocalDateTime startAt;
    private final String title;
    private final MeetingStatus status;
    private final long memberAttendanceCount;
    private final long guestAttendanceCount;
}
