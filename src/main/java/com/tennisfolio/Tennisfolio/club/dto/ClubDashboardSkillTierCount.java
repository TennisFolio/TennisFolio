package com.tennisfolio.Tennisfolio.club.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClubDashboardSkillTierCount {

    private final Long skillTierId;
    private final String name;
    private final int level;
    private final long count;
}
