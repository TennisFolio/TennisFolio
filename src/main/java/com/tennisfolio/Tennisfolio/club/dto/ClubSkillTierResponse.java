package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClubSkillTierResponse {
    private Long id;
    private String name;
    private int level;

    public static ClubSkillTierResponse from(ClubSkillTier skillTier) {
        return new ClubSkillTierResponse(skillTier.getId(), skillTier.getName(), skillTier.getLevel());
    }
}
