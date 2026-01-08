package com.tennisfolio.Tennisfolio.ranking.domain;

import com.tennisfolio.Tennisfolio.common.RankingCategory;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Ranking {

    private Long rankingId;

    private Player player;

    private Long curRank;

    private Long preRank;

    private Long bestRank;

    private Long curPoints;

    private Long prePoints;

    private String lastUpdate;

    private RankingCategory category;

    public Ranking(Long curRank, Long preRank, Long bestRank, Long curPoints, Long prePoints, String lastUpdate, Player player, RankingCategory category){

        this.player = player;
        this.curRank = curRank;
        this.preRank = preRank;
        this.bestRank = bestRank;
        this.curPoints = curPoints;
        this.prePoints = prePoints;
        this.lastUpdate = lastUpdate;
        this.category = category;

    }

    public void updatePlayer(Player player){
        this.player = player;
    }
}
