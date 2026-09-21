package com.tennisfolio.Tennisfolio.club.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClubDashboardMemberParticipation {

    private final long participantCount;
    private final int rate;
    private final ClubDashboardMemberPage members;
}
