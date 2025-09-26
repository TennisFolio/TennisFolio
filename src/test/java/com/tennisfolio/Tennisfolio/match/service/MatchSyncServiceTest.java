package com.tennisfolio.Tennisfolio.match.service;

import com.tennisfolio.Tennisfolio.fixtures.MatchFixtures;
import com.tennisfolio.Tennisfolio.fixtures.RoundFixtures;
import com.tennisfolio.Tennisfolio.fixtures.SeasonInfoFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds.LeagueRoundsDTO;
import com.tennisfolio.Tennisfolio.match.application.MatchSyncService;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakeMatchRepository;
import com.tennisfolio.Tennisfolio.mock.FakeRoundRepository;
import com.tennisfolio.Tennisfolio.mock.leagueEventsByRound.FakeLeagueEventsByRoundApiTemplate;
import com.tennisfolio.Tennisfolio.mock.leagueEventsByRound.FakeLeagueEventsByRoundMapper;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class MatchSyncServiceTest {
    private MatchSyncService matchSyncService;

    private ApiCaller fakeApiCaller = new FakeApiCaller();

    private ApiWorker apiWorker;

    private RoundRepository fakeRoundRepository = new FakeRoundRepository();

    private MatchRepository fakeMatchRepository = new FakeMatchRepository();
    private List<StrategyApiTemplate<?, ?>> strategies = new ArrayList<>();

    @Mock
    private ResponseParser parser;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        EntityMapper<List<LeagueEventsByRoundDTO>, List<Match>> fakeLeagueEventsByRoundMapper = new FakeLeagueEventsByRoundMapper();

        StrategyApiTemplate<List<LeagueEventsByRoundDTO>, List<Match>> fakeLeagueEventsByRoundApi
                = new FakeLeagueEventsByRoundApiTemplate(fakeApiCaller, parser, fakeLeagueEventsByRoundMapper, RapidApi.LEAGUEEVENETBYROUND);

        fakeRoundRepository.collect(List.of(RoundFixtures.wimbledonMen2025Final(), RoundFixtures.wimbledonMen2025SemiFinal(), RoundFixtures.rolandGarrosMen2025Final(), RoundFixtures.rolandGarrosMen2025SemiFinal()));
        fakeRoundRepository.flushAll();

        strategies.add(fakeLeagueEventsByRoundApi);

        apiWorker = new ApiWorker(strategies);

        matchSyncService = new MatchSyncService(fakeRoundRepository, fakeMatchRepository, apiWorker);

    }

    @Test
    public void 매치_리스트_정보_저장_확인(){
        assertThat(fakeMatchRepository.findAll()).isEmpty();

        matchSyncService.saveMatchList();

        List<Match> saved = fakeMatchRepository.findAll();

        assertThat(saved).isNotEmpty();
        assertThat(saved).hasSize(4);
        assertThat(saved).anyMatch(t -> t.getRapidMatchId().equals("1"));
        assertThat(saved).anyMatch(t -> t.getRapidMatchId().equals("2"));
    }

    @Test
    public void 매치_리스트_존재시_저장_안함(){
        List<Match> matchList = List.of(MatchFixtures.rolandGarrosMen2025SemiFinalMatch(), MatchFixtures.rolandGarrosMen2025FinalMatch());

        fakeMatchRepository.collect(matchList);
        fakeMatchRepository.flushAll();

        matchSyncService.saveMatchList();

        List<Match> saved = fakeMatchRepository.findAll();

        assertThat(saved).hasSize(4);  // 중복 제거 확인
        assertThat(saved)
                .extracting(Match::getMatchId, Match::getRapidMatchId)
                .containsExactlyInAnyOrder(
                        tuple(1L, "1"),
                        tuple(2L, "2"),
                        tuple(3L, "3"),
                        tuple(4L, "4")
                );
    }

}
