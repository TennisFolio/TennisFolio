package com.tennisfolio.Tennisfolio.club.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ClubDashboardMemberComposition {

    private final List<ClubDashboardGenderCount> genderCounts;
    private final List<ClubDashboardSkillTierCount> skillTierCounts;
    private final long unclassifiedSkillMemberCount;
}
