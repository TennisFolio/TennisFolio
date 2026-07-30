package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClubDetailResponse {
    private String publicId;
    private String name;
    private String description;
    private String currentUserRole;
    private Boolean admin;
    private Long memberCount;
    private List<ClubSkillTierResponse> skillTiers;

    public ClubDetailResponse(
            String publicId,
            String name,
            String description,
            String currentUserRole,
            Boolean admin,
            Long memberCount
    ) {
        this(publicId, name, description, currentUserRole, admin, memberCount, List.of());
    }

    public static ClubDetailResponse from(
            Club club,
            ClubMember currentMember,
            long memberCount,
            List<ClubSkillTierResponse> skillTiers
    ) {
        return new ClubDetailResponse(
                club.getPublicId(),
                club.getName(),
                club.getDescription(),
                currentMember.getRole().name(),
                currentMember.getRole() == ClubMemberRole.ADMIN,
                memberCount,
                skillTiers
        );
    }
}
