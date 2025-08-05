package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakeRankingRepository;
import com.tennisfolio.Tennisfolio.mock.atpRanking.FakeAtpRankingApiTemplate;
import com.tennisfolio.Tennisfolio.mock.atpRanking.FakeAtpRankingEntityMapper;
import com.tennisfolio.Tennisfolio.mock.atpRanking.FakeAtpRankingResponseParser;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RankingSyncServiceTest {

    private RankingSyncService rankingSyncService;
    ApiCaller fakeApiCaller = new FakeApiCaller();
    EntityMapper<List<AtpRankingApiDTO>, List<Ranking>> fakeAtpRankingEntityMapper = new FakeAtpRankingEntityMapper();
    ResponseParser<List<AtpRankingApiDTO>> fakeAtpRankingResponseParser = new FakeAtpRankingResponseParser();
    StrategyApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> fakeAtpRankingApiTemplate = new FakeAtpRankingApiTemplate(fakeApiCaller, fakeAtpRankingResponseParser, fakeAtpRankingEntityMapper, RapidApi.ATPRANKINGS);
    RankingRepository rankingRepository = new FakeRankingRepository();

    @BeforeEach
    void setUp(){
        this.rankingSyncService = RankingSyncService.builder()
                .rankingApiTemplate(fakeAtpRankingApiTemplate)
                .rankingRepository(rankingRepository)
                .build();

    }
    @Test
    void batch를_통해_모두_저장_성공(){

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
