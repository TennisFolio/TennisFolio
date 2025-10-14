package com.tennisfolio.Tennisfolio.match.service;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.fixtures.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules.EventSchedulesDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.TournamentDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds.LeagueRoundsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo.LeagueSeasonInfoDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails.LeagueDetailsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;
import com.tennisfolio.Tennisfolio.match.application.MatchSyncService;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.mock.*;
import com.tennisfolio.Tennisfolio.mock.eventschedules.FakeEventSchedulesApiTemplate;
import com.tennisfolio.Tennisfolio.mock.eventschedules.FakeEventSchedulesMapper;
import com.tennisfolio.Tennisfolio.mock.leagueDetails.FakeLeagueDetails;
import com.tennisfolio.Tennisfolio.mock.leagueDetails.FakeLeagueDetailsMapper;
import com.tennisfolio.Tennisfolio.mock.leagueEventsByRound.FakeLeagueEventsByRoundApiTemplate;
import com.tennisfolio.Tennisfolio.mock.leagueEventsByRound.FakeLeagueEventsByRoundMapper;
import com.tennisfolio.Tennisfolio.mock.leagueSeasonInfo.FakeLeagueSeasonInfoApiTemplate;
import com.tennisfolio.Tennisfolio.mock.leagueSeasonInfo.FakeLeagueSeasonInfoMapper;
import com.tennisfolio.Tennisfolio.mock.tournamentInfo.FakeTournamentInfo;
import com.tennisfolio.Tennisfolio.mock.tournamentInfo.FakeTournamentInfoMapper;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
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
import static org.assertj.core.groups.Tuple.tuple;

public class MatchSyncServiceTest {
    private MatchSyncService matchSyncService;

    private ApiCaller fakeApiCaller = new FakeApiCaller();

    private ApiWorker apiWorker;

    private CategoryRepository fakeCategoryRepository = new FakeCategoryRepository();

    private TournamentRepository fakeTournamentRepository = new FakeTournamentRepository();

    private SeasonRepository fakeSeasonRepository = new FakeSeasonRepository();

    private RoundRepository fakeRoundRepository = new FakeRoundRepository();

    private MatchRepository fakeMatchRepository = new FakeMatchRepository();
    private List<StrategyApiTemplate<?, ?>> strategies = new ArrayList<>();

    @Mock
    private PlayerProvider playerProvider;

    @Mock
    private ResponseParser parser;



    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        EntityMapper<List<LeagueEventsByRoundDTO>, List<Match>> fakeLeagueEventsByRoundMapper = new FakeLeagueEventsByRoundMapper();

        StrategyApiTemplate<List<LeagueEventsByRoundDTO>, List<Match>> fakeLeagueEventsByRoundApi
                = new FakeLeagueEventsByRoundApiTemplate(fakeApiCaller, parser, fakeLeagueEventsByRoundMapper, RapidApi.LEAGUEEVENETBYROUND);

        EntityMapper<List<EventSchedulesDTO>, List<Match>> fakeEventSchedulesMapper = new FakeEventSchedulesMapper();

        StrategyApiTemplate<List<EventSchedulesDTO>, List<Match>> fakeEventSchedulesApi = new FakeEventSchedulesApiTemplate(fakeApiCaller, parser, fakeEventSchedulesMapper, RapidApi.EVENTSCHEDULES);

        EntityMapper<TournamentInfoDTO, Tournament> fakeTournamentInfoMapper = new FakeTournamentInfoMapper();
        StrategyApiTemplate<TournamentInfoDTO, Tournament> fakeTournamentInfoApi = new FakeTournamentInfo(fakeApiCaller, parser, fakeTournamentInfoMapper, RapidApi.TOURNAMENTINFO);

        EntityMapper<LeagueDetailsDTO, Tournament> fakeLeagueDetailsMapper = new FakeLeagueDetailsMapper();
        StrategyApiTemplate<LeagueDetailsDTO, Tournament> fakeLeagueDetailsApi = new FakeLeagueDetails(fakeApiCaller, parser, fakeLeagueDetailsMapper, RapidApi.LEAGUEDETAILS);

        EntityMapper<LeagueSeasonInfoDTO, Season> fakeLeagueSeasonInfoMapper = new FakeLeagueSeasonInfoMapper();
        StrategyApiTemplate<LeagueSeasonInfoDTO, Season> fakeLeagueSeasonInfoApi = new FakeLeagueSeasonInfoApiTemplate(fakeApiCaller, parser, fakeLeagueSeasonInfoMapper, RapidApi.LEAGUESEASONINFO);

        strategies.addAll(List.of(fakeLeagueEventsByRoundApi, fakeEventSchedulesApi, fakeTournamentInfoApi, fakeLeagueDetailsApi, fakeLeagueSeasonInfoApi));

        apiWorker = new ApiWorker(strategies);

        matchSyncService = new MatchSyncService(fakeCategoryRepository, fakeTournamentRepository, fakeSeasonRepository, fakeRoundRepository, fakeMatchRepository, apiWorker, playerProvider);

    }

    @Test
    public void 매치_리스트_정보_저장_확인(){

        fakeRoundRepository.collect(List.of(RoundFixtures.wimbledonMen2025Final(), RoundFixtures.wimbledonMen2025SemiFinal(), RoundFixtures.rolandGarrosMen2025Final(), RoundFixtures.rolandGarrosMen2025SemiFinal()));
        fakeRoundRepository.flushAll();

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
        fakeRoundRepository.collect(List.of(RoundFixtures.wimbledonMen2025Final(), RoundFixtures.wimbledonMen2025SemiFinal(), RoundFixtures.rolandGarrosMen2025Final(), RoundFixtures.rolandGarrosMen2025SemiFinal()));
        fakeRoundRepository.flushAll();

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

    @Test
    public void 이벤트_스케줄_매치_저장(){
        fakeRoundRepository.collect(List.of(RoundFixtures.wimbledonMen2025Final(), RoundFixtures.wimbledonMen2025SemiFinal(), RoundFixtures.rolandGarrosMen2025Final(), RoundFixtures.rolandGarrosMen2025SemiFinal()));
        fakeRoundRepository.flushAll();

        String year = "2025";
        String month = "10";
        String day = "11";

        matchSyncService.saveEventSchedule(year, month, day);

        List<Match> saved = fakeMatchRepository.findAll();

        assertThat(saved).hasSize(4);  // 중복 제거 확인
        assertThat(saved)
                .extracting(Match::getRapidMatchId, Match::getStartTimeStamp)
                .containsExactlyInAnyOrder(
                        tuple("1", "20251011080000"),
                        tuple("2", "20251011100000"),
                        tuple("3", "20251011120000"),
                        tuple("4", "20251011140000")
                );
    }

    @Test
    public void 토너먼트_시즌_라운드가_없으면_새로_DB에_저장(){
        String year = "2025";
        String month = "10";
        String day = "11";

        matchSyncService.saveEventSchedule(year, month, day);

        Tournament findTournament = fakeTournamentRepository.findByRapidTournamentId(TournamentFixtures.wimbledonATP().getRapidTournamentId()).get();
        Season findSeason = fakeSeasonRepository.findByRapidSeasonId(SeasonFixtures.wimbledonMen2025().getRapidSeasonId()).get();
        Round findRound = fakeRoundRepository.findBySeasonAndRoundAndSlug(findSeason,
                RoundFixtures.wimbledonMen2025QuarterFinal().getRound(),
                RoundFixtures.wimbledonMen2025QuarterFinal().getSlug()).get();

        assertThat(findTournament.getTournamentName()).isEqualTo("Wimbledon");
        assertThat(findSeason.getYear()).isEqualTo("2025");
        assertThat(findRound.getName()).isEqualTo("Quarterfinals");


    }

}
