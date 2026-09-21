package com.tennisfolio.Tennisfolio.club.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClubDashboardMemberActivity {

    private final Long memberId;
    private final String memberName;
    private final long attendanceCount;
}
