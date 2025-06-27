package com.tennisfolio.Tennisfolio.ranking.dto;

import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class RankingResponse {
    private Long rankingId;
    private Long curRanking;
    private RankingPlayerResponse player;
    private Long preRanking;
    private Long bestRanking;
    private Long curPoints;
    private Long prePoints;
    private Long gapRanking;
    private Long gapPoints;
    private String rankingLastUpdated;

    public RankingResponse(Ranking ranking){
        this.rankingId = ranking.getRankingId();
        this.player = new RankingPlayerResponse(ranking.getPlayer());
        this.curRanking = ranking.getCurRank();
        this.preRanking = ranking.getPreRank();
        this.gapRanking = ranking.getCurRank() - ranking.getPreRank();
        this.bestRanking = ranking.getBestRank();
        this.curPoints = ranking.getCurPoints();
        this.prePoints = ranking.getPrePoints();
        this.gapPoints = ranking.getCurPoints() - ranking.getPrePoints();
        this.rankingLastUpdated = ranking.getLastUpdate();

    }
}
