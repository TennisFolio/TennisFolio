package com.tennisfolio.Tennisfolio.ranking.domain;

import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_ranking")
@Getter
@NoArgsConstructor
public class Ranking extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="RANKING_ID")
    private Long rankingId;
    @ManyToOne
    @JoinColumn(name = "PLAYER_ID")
    private Player player;
    @Column(name = "CUR_RANKING")
    private Long curRank;
    @Column(name="PRE_RANKING")
    private Long preRank;
    @Column(name="BEST_RANKING")
    private Long bestRank;
    @Column(name="CUR_POINTS")
    private Long curPoints;
    @Column(name="PRE_POINTS")
    private Long prePoints;
    @Column(name="RANKING_LAST_UPDATED")
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

}
