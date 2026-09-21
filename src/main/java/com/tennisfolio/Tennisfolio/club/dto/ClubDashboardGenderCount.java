package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClubDashboardGenderCount {

    private final Gender gender;
    private final long count;
}
