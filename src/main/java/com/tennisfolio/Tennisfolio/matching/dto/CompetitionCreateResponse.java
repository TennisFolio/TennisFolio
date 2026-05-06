package com.tennisfolio.Tennisfolio.matching.dto;

import com.tennisfolio.Tennisfolio.matching.entity.Competition;

public record CompetitionCreateResponse(
        String publicId,
        String editToken
) {
    public static CompetitionCreateResponse from(Competition competition) {
        return new CompetitionCreateResponse(
                competition.getPublicId(),
                competition.getEditToken()
        );
    }
}
