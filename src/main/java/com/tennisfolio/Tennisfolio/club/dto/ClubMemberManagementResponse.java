package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ClubMemberManagementResponse {
    private Long id;
    private Long userId;
    private String name;
    private String gender;
    private String role;
    private String memberStatus;
    private Long skillTierId;
    private String skillTierName;
    private Integer skillTierLevel;
    private LocalDate joinedAt;
    private LocalDate suspendedAt;
    private LocalDate leftAt;
    private String contactMemo;
    private String memo;

    public static ClubMemberManagementResponse from(ClubMember member) {
        ClubSkillTier skillTier = member.getSkillTier();
        return new ClubMemberManagementResponse(
                member.getId(),
                member.getUserId(),
                member.getName(),
                member.getGender().name(),
                member.getRole().name(),
                member.getMemberStatus().name(),
                skillTier == null ? null : skillTier.getId(),
                skillTier == null ? null : skillTier.getName(),
                skillTier == null ? null : skillTier.getLevel(),
                member.getJoinedAt(),
                member.getSuspendedAt(),
                member.getLeftAt(),
                member.getContactMemo(),
                member.getMemo()
        );
    }
}
