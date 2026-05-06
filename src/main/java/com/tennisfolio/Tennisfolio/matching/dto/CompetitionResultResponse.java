package com.tennisfolio.Tennisfolio.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CompetitionResultResponse {
    private String publicId;
    private String name;
    private Integer totalGames;
    private Integer completedGames;
    private Rankings rankings;

    @Getter
    @AllArgsConstructor
    public static class Rankings {
        private List<CompetitionRankingResponse> overall;
        private List<CompetitionRankingResponse> male;
        private List<CompetitionRankingResponse> female;
    }
}
