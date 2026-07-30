package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClubMemberResponse {
    private Long id;
    private Long userId;
    private String name;
    private String gender;
    private String role;
    private Long skillTierId;
    private String skillTierName;
    private Integer skillTierLevel;
    private String contactMemo;
    private String memo;

    public static ClubMemberResponse from(ClubMember member) {
        ClubSkillTier skillTier = member.getSkillTier();
        return new ClubMemberResponse(
                member.getId(),
                member.getUserId(),
                member.getName(),
                member.getGender().name(),
                member.getRole().name(),
                skillTier == null ? null : skillTier.getId(),
                skillTier == null ? null : skillTier.getName(),
                skillTier == null ? null : skillTier.getLevel(),
                member.getContactMemo(),
                member.getMemo()
        );
    }
}
