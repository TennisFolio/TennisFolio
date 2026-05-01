package com.tennisfolio.Tennisfolio.matching.dto;

import com.tennisfolio.Tennisfolio.matching.entity.CompetitionStat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompetitionStatResponse {
    private Integer totalGames;
    private Integer mixedCount;
    private Integer maleCount;
    private Integer femaleCount;
    private Integer m2f2SplitCount;
    private Integer randomM3F1Count;
    private Integer randomM1F3Count;
    private Integer maxGames;
    private Integer minGames;

    public static CompetitionStatResponse from(CompetitionStat stat) {
        if (stat == null) {
            return null;
        }

        return new CompetitionStatResponse(
                stat.getTotalGames(),
                stat.getMixedCount(),
                stat.getMaleCount(),
                stat.getFemaleCount(),
                stat.getM2f2SplitCount(),
                stat.getRandomM3F1Count(),
                stat.getRandomM1F3Count(),
                stat.getMaxGames(),
                stat.getMinGames()
        );
    }
}
