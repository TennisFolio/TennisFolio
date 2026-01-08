package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakePlayerRepository;
import com.tennisfolio.Tennisfolio.mock.FakeRankingRepository;
import com.tennisfolio.Tennisfolio.mock.atpRanking.FakeAtpRankingApiTemplate;
import com.tennisfolio.Tennisfolio.mock.atpRanking.FakeAtpRankingEntityMapper;
import com.tennisfolio.Tennisfolio.mock.teamDetails.FakeTeamDetailsApiTemplate;
import com.tennisfolio.Tennisfolio.mock.teamDetails.FakeTeamDetailsEntityMapper;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class RankingSyncServiceTest {

    RankingSyncService rankingSyncService;
    ApiCaller fakeApiCaller = new FakeApiCaller();
    EntityMapper<List<AtpRankingApiDTO>, List<Ranking>> fakeAtpRankingEntityMapper = new FakeAtpRankingEntityMapper();
    @Mock
    ResponseParser parser;

    RankingRepository rankingRepository = new FakeRankingRepository();
    PlayerRepository playerRepository = new FakePlayerRepository();

    EntityMapper<TeamDetailsApiDTO, PlayerAggregate> fakeTeamDetailsMapper = new FakeTeamDetailsEntityMapper();

    @Mock
    PlayerImageService playerImageService;

    @Mock
    private ApiCallCounter apiCallCounter;

    @Mock
    private RedisRateLimiter redisRateLimiter;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        StrategyApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> fakeAtpRankingApiTemplate = new FakeAtpRankingApiTemplate(fakeApiCaller, parser, fakeAtpRankingEntityMapper, apiCallCounter, RapidApi.ATPRANKINGS);
        StrategyApiTemplate<TeamDetailsApiDTO, PlayerAggregate> teamDetailsApi = new FakeTeamDetailsApiTemplate(fakeApiCaller, parser, fakeTeamDetailsMapper, apiCallCounter, RapidApi.TEAMDETAILS);

        List<StrategyApiTemplate<?,?>> strategyApiTemplates = new ArrayList<>();
        strategyApiTemplates.add(fakeAtpRankingApiTemplate);
        strategyApiTemplates.add(teamDetailsApi);
        ApiWorker apiWorker = new ApiWorker(strategyApiTemplates, redisRateLimiter);
        when(playerImageService.fetchImage("1")).thenReturn("/player/1");
        when(playerImageService.fetchImage("2")).thenReturn("/player/2");
        when(playerImageService.fetchImage("3")).thenReturn("/player/3");

        PlayerProvider playerProvider = new PlayerProvider(playerRepository, playerImageService, apiWorker);
        this.rankingSyncService = RankingSyncService.builder()
                .apiWorker(apiWorker)
                .rankingRepository(rankingRepository)
                .playerProvider(playerProvider)
                .build();

    }
    @Test
    void batch를_통해_모두_저장_성공(){
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

        playerRepository.saveAll(List.of(player1, player2, player3));
        rankingSyncService.saveAtpRanking();

        List<Ranking> rankings = rankingRepository.findByLastUpdate("20250731");

        Ranking ranking1 = rankings.get(0);
        Ranking ranking2 = rankings.get(1);
        Ranking ranking3 = rankings.get(2);

        assertThat(ranking1.getRankingId()).isEqualTo(1L);
        assertThat(ranking2.getRankingId()).isEqualTo(2L);
        assertThat(ranking3.getRankingId()).isEqualTo(3L);
    }




}
