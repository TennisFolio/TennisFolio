package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.mock.FakeRankingRepository;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.dto.RankingResponse;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RankingServiceTest {
    private RankingService rankingService;
    private RankingRepository fakeRankingRepository = new FakeRankingRepository();

    @BeforeEach
    void setUp() {
        this.rankingService = RankingService.builder()
                .rankingRepository(fakeRankingRepository)
                .build();

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

        fakeRankingRepository.save(ranking1);
        fakeRankingRepository.save(ranking2);
        fakeRankingRepository.save(ranking3);
    }
    @Test
    void 가장_최근_랭킹을_페이지와_사이즈로_조회한다(){

        List<RankingResponse> rankings = rankingService.getRanking(0, 2);
        RankingResponse rankings1 = rankings.get(0);
        RankingResponse rankings2 = rankings.get(1);

        assertThatThrownBy(() -> rankings.get(2)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(rankings1.getRankingId()).isEqualTo(1L);
        assertThat(rankings1.getCurRanking()).isEqualTo(1L);
        assertThat(rankings1.getBestRanking()).isEqualTo(1L);
        assertThat(rankings1.getPreRanking()).isEqualTo(1L);
        assertThat(rankings1.getCurPoints()).isEqualTo(1000L);
        assertThat(rankings1.getPrePoints()).isEqualTo(900L);
        assertThat(rankings1.getRankingLastUpdated()).isEqualTo("20250731");
        assertThat(rankings1.getPlayer().getPlayerId()).isEqualTo(1L);

        assertThat(rankings2.getRankingId()).isEqualTo(2L);
        assertThat(rankings2.getCurRanking()).isEqualTo(2L);
        assertThat(rankings2.getBestRanking()).isEqualTo(1L);
        assertThat(rankings2.getPreRanking()).isEqualTo(2L);
        assertThat(rankings2.getCurPoints()).isEqualTo(900L);
        assertThat(rankings2.getPrePoints()).isEqualTo(800L);
        assertThat(rankings2.getRankingLastUpdated()).isEqualTo("20250731");
        assertThat(rankings2.getPlayer().getPlayerId()).isEqualTo(2L);
    }
}
