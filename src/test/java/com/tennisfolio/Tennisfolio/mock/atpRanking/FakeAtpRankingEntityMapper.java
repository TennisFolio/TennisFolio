package com.tennisfolio.Tennisfolio.mock.atpRanking;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;

import java.util.ArrayList;
import java.util.List;

public class FakeAtpRankingEntityMapper implements EntityMapper<List<AtpRankingApiDTO>, List<Ranking>> {

    @Override
    public List<Ranking> map(List<AtpRankingApiDTO> dto, Object... params) {
        List<Ranking> rankings = new ArrayList<>();

        Player player1 = Player.builder()
                .playerId(1L)
                .rapidPlayerId("1")
                .playerName("Alcaraz")
                .build();

        Player player2 = Player.builder()
                .playerId(2L)
                .rapidPlayerId("2")
                .playerName("Djokobic")
                .build();

        Player player3 = Player.builder()
                .playerId(3L)
                .rapidPlayerId("3")
                .playerName("Sinner")
                .build();

        Ranking ranking1 = Ranking.builder()
                .rankingId(1L)
                .curRank(1L)
                .bestRank(1L)
                .preRank(1L)
                .curPoints(1000L)
                .prePoints(900L)
                .lastUpdate("20250731")
                .player(player1)
                .build();

        Ranking ranking2 = Ranking.builder()
                .rankingId(2L)
                .curRank(2L)
                .bestRank(1L)
                .preRank(2L)
                .curPoints(900L)
                .prePoints(800L)
                .lastUpdate("20250731")
                .player(player2)
                .build();

        Ranking ranking3 = Ranking.builder()
                .rankingId(3L)
                .curRank(3L)
                .bestRank(1L)
                .preRank(3L)
                .curPoints(800L)
                .prePoints(700L)
                .lastUpdate("20250731")
                .player(player3)
                .build();

        rankings.add(ranking1);
        rankings.add(ranking2);
        rankings.add(ranking3);

        return rankings;
    }
}

