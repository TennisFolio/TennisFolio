package com.tennisfolio.Tennisfolio.matching.dto;

import com.tennisfolio.Tennisfolio.matching.entity.CompetitionStat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameBalanceResponse {
    private Integer maxGames;
    private Integer minGames;
    private Integer difference;
    private String message;

    public static GameBalanceResponse from(CompetitionStat stat) {
        int maxGames = stat.getMaxGames();
        int minGames = stat.getMinGames();
        int difference = maxGames - minGames;
        String message = difference == 0
                ? "1인당 게임수가 동일해요."
                : "1인당 게임수가 최대 " + difference + "경기 차이나요.";

        return new GameBalanceResponse(maxGames, minGames, difference, message);
    }
}
