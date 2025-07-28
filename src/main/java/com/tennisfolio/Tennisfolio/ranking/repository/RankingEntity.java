package com.tennisfolio.Tennisfolio.ranking.repository;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerEntity;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_ranking")
@Getter
@NoArgsConstructor
public class RankingEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="RANKING_ID")
    private Long rankingId;
    @ManyToOne
    @JoinColumn(name = "PLAYER_ID")
    private PlayerEntity playerEntity;
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

    public static RankingEntity fromModel(Ranking ranking) {
        RankingEntity rankingEntity = new RankingEntity();
        rankingEntity.rankingId = ranking.getRankingId();
        rankingEntity.playerEntity = PlayerEntity.fromModel(ranking.getPlayer());
        rankingEntity.curRank = ranking.getCurRank();
        rankingEntity.preRank = ranking.getPreRank();
        rankingEntity.bestRank = ranking.getBestRank();
        rankingEntity.curPoints = ranking.getCurPoints();
        rankingEntity.prePoints = ranking.getPrePoints();
        rankingEntity.lastUpdate = ranking.getLastUpdate();

        return rankingEntity;
    }

    public Ranking toModel(){
        return Ranking.builder()
                .rankingId(rankingId)
                .player(playerEntity.toModel())
                .curRank(curRank)
                .preRank(preRank)
                .bestRank(bestRank)
                .curPoints(curPoints)
                .prePoints(prePoints)
                .lastUpdate(lastUpdate)
                .build();
    }

}
