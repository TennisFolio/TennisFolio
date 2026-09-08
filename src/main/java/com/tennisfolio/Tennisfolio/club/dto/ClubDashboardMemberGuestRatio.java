package com.tennisfolio.Tennisfolio.club.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClubDashboardMemberGuestRatio {

    private final long memberAttendanceCount;
    private final long guestAttendanceCount;
    private final int memberRate;
    private final int guestRate;
}
