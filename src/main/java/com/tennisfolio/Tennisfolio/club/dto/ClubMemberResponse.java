package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ClubMemberResponse {
    private Long id;
    private String name;
    private String gender;
    private Long skillTierId;
    private String skillTierName;
    private Integer skillTierLevel;
    private String contactMemo;
    private String memo;

    public static ClubMemberResponse from(ClubMember member) {
        ClubSkillTier tier = member.getSkillTier();
        return new ClubMemberResponse(member.getId(), member.getName(), member.getGender().name(),
                tier == null ? null : tier.getId(), tier == null ? null : tier.getName(),
                tier == null ? null : tier.getLevel(), member.getContactMemo(), member.getMemo());
    }
}
