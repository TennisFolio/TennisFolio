package com.tennisfolio.Tennisfolio.player.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlayerDetailResponse {
    private Long playerId;
    private String rapidPlayerId;
    private String playerName;
    private String playerNameKr;
    private String birth;
    private String countryCode;
    private String turnedPro;
    private String weight;
    private String plays;
    private String height;
    private String image;
    private Long prizeCurrentAmount;
    private String prizeCurrentCurrency;
    private Long prizeTotalAmount;
    private String prizeTotalCurrency;
    private Long rankingId;
    private Long curRanking;
    private Long curPoints;
    private Long bestRank;

    @Builder
    public PlayerDetailResponse(
            Long playerId,
            String rapidPlayerId,
            String playerName,
            String playerNameKr,
            String birth,
            String countryCode,
            String turnedPro,
            String weight,
            String plays,
            String height,
            String image,
            Long prizeCurrentAmount,
            String prizeCurrentCurrency,
            Long prizeTotalAmount,
            String prizeTotalCurrency,
            Long rankingId,
            Long curRanking,
            Long curPoints,
            Long bestRank
    ) {
        this.playerId = playerId;
        this.rapidPlayerId = rapidPlayerId;
        this.playerName = playerName;
        this.playerNameKr = playerNameKr;
        this.birth = birth;
        this.countryCode = countryCode;
        this.turnedPro = turnedPro;
        this.weight = weight;
        this.plays = plays;
        this.height = String.valueOf((int)(Double.parseDouble(height) * 100));
        this.image = image;
        this.prizeCurrentAmount = prizeCurrentAmount;
        this.prizeCurrentCurrency = prizeCurrentCurrency;
        this.prizeTotalAmount = prizeTotalAmount;
        this.prizeTotalCurrency = prizeTotalCurrency;
        this.rankingId = rankingId;
        this.curRanking = curRanking;
        this.curPoints = curPoints;
        this.bestRank = bestRank;
    }
}
