package com.tennisfolio.Tennisfolio.matching.dto;

import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompetitionCreateResponse {
    private String publicId;
    private String competitionAdminToken;

    public static CompetitionCreateResponse from(Competition competition, String competitionAdminToken) {
        return new CompetitionCreateResponse(
                competition.getPublicId(),
                competitionAdminToken
        );
    }
}
