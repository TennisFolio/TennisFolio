package com.tennisfolio.Tennisfolio.round.service;

import com.tennisfolio.Tennisfolio.fixtures.RoundFixtures;
import com.tennisfolio.Tennisfolio.fixtures.SeasonFixtures;
import com.tennisfolio.Tennisfolio.fixtures.SeasonInfoFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds.LeagueRoundsDTO;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakeRoundRepository;
import com.tennisfolio.Tennisfolio.mock.FakeSeasonRepository;
import com.tennisfolio.Tennisfolio.mock.leagueRounds.FakeLeagueRoundsApiTemplate;
import com.tennisfolio.Tennisfolio.mock.leagueRounds.FakeLeagueRoundsMapper;
import com.tennisfolio.Tennisfolio.round.application.RoundSyncService;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RoundSyncServiceTest {

    private RoundSyncService roundSyncService;

    private RoundRepository fakeRoundRepository = new FakeRoundRepository();

    private SeasonRepository fakeSeasonRepository = new FakeSeasonRepository();

    private ApiCaller apiCaller = new FakeApiCaller();

    private List<StrategyApiTemplate<?, ?>> strategies = new ArrayList<>();

    private StrategyApiTemplate<List<LeagueRoundsDTO>, List<Round>> leagueRoundsApiTemplate;

    @Mock
    private ApiCallCounter apiCallCounter;

    @Mock
    private ResponseParser parser;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        EntityMapper<List<LeagueRoundsDTO>, List<Round>> fakeLeagueRoundsMapper = new FakeLeagueRoundsMapper();

        leagueRoundsApiTemplate = new FakeLeagueRoundsApiTemplate(apiCaller, parser, fakeLeagueRoundsMapper, apiCallCounter, RapidApi.LEAGUEROUNDS);

        strategies.add(leagueRoundsApiTemplate);

        ApiWorker apiWorker = new ApiWorker(strategies);

        fakeSeasonRepository.collect(List.of(SeasonInfoFixtures.wimbledonMen2025(), SeasonInfoFixtures.rolandGarrosMen2025()));
        fakeSeasonRepository.flushAll();

        roundSyncService = new RoundSyncService(apiWorker, fakeSeasonRepository, fakeRoundRepository);
    }

    @Test
    void 라운드_리스트_정보_저장_확인(){
        assertThat(fakeRoundRepository.findAll()).isEmpty();

        roundSyncService.saveRoundList();

        List<Round> saved = fakeRoundRepository.findAll();

        assertThat(saved).isNotEmpty();
        assertThat(saved).anyMatch(t -> t.getName().equals("Semifinals"));
        assertThat(saved).anyMatch(t -> t.getName().equals("Final"));
    }

    @Test
    void 라운드_저장_중복시_넘어감(){
        List<Round> roundList = List.of(RoundFixtures.wimbledonMen2025SemiFinal(), RoundFixtures.wimbledonMen2025Final());

        fakeRoundRepository.collect(roundList);
        fakeRoundRepository.flushAll();

        roundSyncService.saveRoundList();

        List<Round> saved = fakeRoundRepository.findAll();

        assertThat(saved).hasSize(4);  // 중복 제거 확인
        assertThat(saved.get(0).getRoundId()).isEqualTo(roundList.get(0).getRoundId());
        assertThat(saved.get(1).getRoundId()).isEqualTo(roundList.get(1).getRoundId());

    }

}
