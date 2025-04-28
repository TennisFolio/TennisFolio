package com.tennisfolio.Tennisfolio.ranking.response;

import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RankingResponse {
    private Long rankingId;
    private Long curRanking;
    private RankingPlayerResponse player;
    private Long preRanking;
    private Long bestRanking;
    private Long curPoints;
    private Long prePoints;
    private String rankingLastUpdated;

    public RankingResponse(Ranking ranking){
        this.rankingId = ranking.getRankingId();
        this.player = new RankingPlayerResponse(ranking.getPlayer());
        this.curRanking = ranking.getCurRank();
        this.preRanking = ranking.getPreRank();
        this.bestRanking = ranking.getBestRank();
        this.curPoints = ranking.getCurPoints();
        this.prePoints = ranking.getPrePoints();
        this.rankingLastUpdated = ranking.getLastUpdate();

    }
}
