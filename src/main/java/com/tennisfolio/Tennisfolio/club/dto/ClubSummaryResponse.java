package com.tennisfolio.Tennisfolio.club.dto;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClubSummaryResponse {
    private String publicId;
    private String name;
    private String description;
    private String currentUserRole;
    private Long memberCount;

    public static ClubSummaryResponse from(ClubMember currentMember, long memberCount) {
        Club club = currentMember.getClub();
        return new ClubSummaryResponse(
                club.getPublicId(),
                club.getName(),
                club.getDescription(),
                currentMember.getRole().name(),
                memberCount
        );
    }
}
