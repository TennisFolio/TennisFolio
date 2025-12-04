package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;

public class RankingFixtures {
    public static Ranking ranking1(){
        return Ranking.builder()
                .rankingId(1L)
                .player(PlayerFixtures.alcaraz())
                .curRank(1L)
                .bestRank(1L)
                .preRank(1L)
                .curPoints(12050L)
                .prePoints(12050L)
                .lastUpdate("20251201")
                .build();
    }

    public static Ranking ranking2(){
        return Ranking.builder()
                .rankingId(2L)
                .player(PlayerFixtures.sinner())
                .curRank(2L)
                .bestRank(1L)
                .preRank(2L)
                .curPoints(11500L)
                .prePoints(11500L)
                .lastUpdate("20251201")
                .build();
    }

    public static Ranking ranking3(){
        return Ranking.builder()
                .rankingId(3L)
                .player(PlayerFixtures.shelton())
                .curRank(3L)
                .bestRank(1L)
                .preRank(3L)
                .curPoints(9000L)
                .prePoints(8500L)
                .lastUpdate("20251201")
                .build();
    }


}
