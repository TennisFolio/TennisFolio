package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClubDetailResponse {
    private String publicId;
    private String name;
    private String description;
    private String currentUserRole;
    private Boolean admin;
    private Long memberCount;

    public static ClubDetailResponse from(Club club, ClubMember currentMember, long memberCount) {
        return new ClubDetailResponse(
                club.getPublicId(),
                club.getName(),
                club.getDescription(),
                currentMember.getRole().name(),
                currentMember.getRole() == ClubMemberRole.ADMIN,
                memberCount
        );
    }
}
