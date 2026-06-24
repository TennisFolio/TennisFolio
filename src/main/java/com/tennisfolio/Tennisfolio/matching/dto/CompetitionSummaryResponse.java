package com.tennisfolio.Tennisfolio.matching.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CompetitionSummaryResponse {
    private String publicId;
    private String name;
    private Integer maleCount;
    private Integer femaleCount;
    private Integer courtCount;
    private Integer rounds;
    private String status;
    private String mode;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static CompetitionSummaryResponse from(Competition competition) {
        return new CompetitionSummaryResponse(
                competition.getPublicId(),
                competition.getName(),
                competition.getMaleCount(),
                competition.getFemaleCount(),
                competition.getCourtCount(),
                competition.getRounds(),
                competition.getStatus().name(),
                competition.getMode() == null
                        ? Competition.CompetitionMode.FIXED_SCHEDULE.name()
                        : competition.getMode().name(),
                competition.getCreateDt()
        );
    }
}
