package com.tennisfolio.Tennisfolio.matching.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;

import java.time.LocalDateTime;

public record CompetitionUpdateResponse(
        String publicId,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {
    public static CompetitionUpdateResponse from(Competition competition) {
        return new CompetitionUpdateResponse(
                competition.getPublicId(),
                competition.getName(),
                competition.getCreateDt()
        );
    }
}
