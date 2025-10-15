package com.tennisfolio.Tennisfolio.ranking.domain;

import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerEntity;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import jakarta.persistence.*;
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

    public Ranking(AtpRankingApiDTO rapidDTO, Player player){

        this.player = player;
        this.curRank = rapidDTO.getCurRank();
        this.preRank = rapidDTO.getPreRank();
        this.bestRank = rapidDTO.getBestRank();
        this.curPoints = rapidDTO.getPoint();
        this.prePoints = rapidDTO.getPrePoints();
        this.lastUpdate = rapidDTO.getUpdateTime();

    }

    public void updatePlayer(Player player){
        this.player = player;
    }
}
