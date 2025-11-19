package com.tennisfolio.Tennisfolio.statistic.service;

import com.tennisfolio.Tennisfolio.fixtures.MatchFixtures;
import com.tennisfolio.Tennisfolio.fixtures.SeasonFixtures;
import com.tennisfolio.Tennisfolio.fixtures.SeasonInfoFixtures;
import com.tennisfolio.Tennisfolio.fixtures.StatisticFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics.EventsStatisticsDTO;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakeMatchRepository;
import com.tennisfolio.Tennisfolio.mock.FakeStatisticRepository;
import com.tennisfolio.Tennisfolio.mock.eventstatistics.FakeEventStatisticsApiTemplate;
import com.tennisfolio.Tennisfolio.mock.eventstatistics.FakeEventsStatisticsMapper;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.statistic.application.StatisticSyncService;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import com.tennisfolio.Tennisfolio.statistic.repository.StatisticRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StatisticSyncServiceTest {

    private StatisticSyncService statisticService;

    private StatisticRepository fakeStatisticRepository = new FakeStatisticRepository();

    private MatchRepository fakeMatchRepository = new FakeMatchRepository();

    private ApiWorker apiWorker;

    @Mock
    private ResponseParser parser;

    @Mock
    private ApiCallCounter apiCallCounter;

    @Mock
    private RedisRateLimiter redisRateLimiter;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        ApiCaller apiCaller = new FakeApiCaller();

        List<StrategyApiTemplate<?, ?>> strategies = new ArrayList<>();

        EntityMapper<List<EventsStatisticsDTO>, List<Statistic>> eventsStatisticMapper = new FakeEventsStatisticsMapper();
        StrategyApiTemplate<List<EventsStatisticsDTO>, List<Statistic>> eventsStatisticApiTemplate = new FakeEventStatisticsApiTemplate(apiCaller, parser, eventsStatisticMapper, apiCallCounter, RapidApi.EVENTSTATISTICS);

        strategies.add(eventsStatisticApiTemplate);

        fakeMatchRepository.collect(List.of(MatchFixtures.wimbledonMen2025FinalMatch(), MatchFixtures.rolandGarrosMen2025FinalMatch()));
        fakeMatchRepository.flushAll();
        apiWorker = new ApiWorker(strategies, redisRateLimiter);
        statisticService = new StatisticSyncService(apiWorker, fakeMatchRepository, fakeStatisticRepository);
    }

    @Test
    void 통계_리스트_정보_저장_확인(){
        assertThat(fakeStatisticRepository.findAll()).isEmpty();

        statisticService.saveStatisticList();

        List<Statistic> saved = fakeStatisticRepository.findAll();

        assertThat(saved).isNotEmpty();
        assertThat(saved).anyMatch(t -> t.getMetric().equals("firstServeAccuracy"));
        assertThat(saved).anyMatch(t -> t.getMetric().equals("secondServeAccuracy"));
        assertThat(saved).anyMatch(t -> t.getMetric().equals("firstReturnPoints"));
        assertThat(saved).anyMatch(t -> t.getMetric().equals("doubleFaults"));
    }

    @Test
    void 시즌_저장_중복시_넘어감(){
        List<Statistic> statisticList = List.of(StatisticFixtures.allFirstServe(), StatisticFixtures.allSecondServe());

        fakeStatisticRepository.collect(statisticList);
        fakeStatisticRepository.flushAll();

        statisticService.saveStatisticList();

        List<Statistic> saved = fakeStatisticRepository.findAll();

        assertThat(saved).hasSize(4);  // 중복 제거 확인
        assertThat(saved.get(0).getStatId()).isEqualTo(statisticList.get(0).getStatId());
        assertThat(saved.get(1).getStatId()).isEqualTo(statisticList.get(1).getStatId());

    }
}
