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
import com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics.EventsStatisticsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails.LeagueDetailsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;
import com.tennisfolio.Tennisfolio.match.application.MatchSyncService;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.mock.*;
import com.tennisfolio.Tennisfolio.mock.eventschedules.FakeEventSchedulesApiTemplate;
import com.tennisfolio.Tennisfolio.mock.eventschedules.FakeEventSchedulesMapper;
import com.tennisfolio.Tennisfolio.mock.eventstatistics.FakeEventStatisticsApiTemplate;
import com.tennisfolio.Tennisfolio.mock.eventstatistics.FakeEventsStatisticsMapper;
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
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import com.tennisfolio.Tennisfolio.statistic.repository.StatisticRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
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

    private StatisticRepository fakeStatisticRepository = new FakeStatisticRepository();

    private List<StrategyApiTemplate<?, ?>> strategies = new ArrayList<>();

    @Mock
    private PlayerProvider playerProvider;

    @Mock
    private ResponseParser parser;

    @Mock
    private ApiCallCounter apiCallCounter;

    @Mock
    private RedisRateLimiter redisRateLimiter;

    Clock fixedClock = Clock.fixed(
            LocalDate.of(2025, 10, 9).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );



    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        EntityMapper<List<LeagueEventsByRoundDTO>, List<Match>> fakeLeagueEventsByRoundMapper = new FakeLeagueEventsByRoundMapper();

        StrategyApiTemplate<List<LeagueEventsByRoundDTO>, List<Match>> fakeLeagueEventsByRoundApi
                = new FakeLeagueEventsByRoundApiTemplate(fakeApiCaller, parser, fakeLeagueEventsByRoundMapper, apiCallCounter, RapidApi.LEAGUEEVENETBYROUND);

        EntityMapper<List<EventSchedulesDTO>, List<Match>> fakeEventSchedulesMapper = new FakeEventSchedulesMapper();

        StrategyApiTemplate<List<EventSchedulesDTO>, List<Match>> fakeEventSchedulesApi = new FakeEventSchedulesApiTemplate(fakeApiCaller, parser, fakeEventSchedulesMapper,apiCallCounter, RapidApi.EVENTSCHEDULES);

        EntityMapper<TournamentInfoDTO, Tournament> fakeTournamentInfoMapper = new FakeTournamentInfoMapper();
        StrategyApiTemplate<TournamentInfoDTO, Tournament> fakeTournamentInfoApi = new FakeTournamentInfo(fakeApiCaller, parser, fakeTournamentInfoMapper,apiCallCounter,RapidApi.TOURNAMENTINFO);

        EntityMapper<LeagueDetailsDTO, Tournament> fakeLeagueDetailsMapper = new FakeLeagueDetailsMapper();
        StrategyApiTemplate<LeagueDetailsDTO, Tournament> fakeLeagueDetailsApi = new FakeLeagueDetails(fakeApiCaller, parser, fakeLeagueDetailsMapper,apiCallCounter, RapidApi.LEAGUEDETAILS);

        EntityMapper<LeagueSeasonInfoDTO, Season> fakeLeagueSeasonInfoMapper = new FakeLeagueSeasonInfoMapper();
        StrategyApiTemplate<LeagueSeasonInfoDTO, Season> fakeLeagueSeasonInfoApi = new FakeLeagueSeasonInfoApiTemplate(fakeApiCaller, parser, fakeLeagueSeasonInfoMapper,apiCallCounter, RapidApi.LEAGUESEASONINFO);

        EntityMapper<List<EventsStatisticsDTO>, List<Statistic>> fakeEventsStatisticsMapper = new FakeEventsStatisticsMapper();
        StrategyApiTemplate<List<EventsStatisticsDTO>, List<Statistic>> fakeEventStatisticsApi = new FakeEventStatisticsApiTemplate(fakeApiCaller, parser, fakeEventsStatisticsMapper, apiCallCounter, RapidApi.EVENTSTATISTICS);

        strategies.addAll(List.of(fakeLeagueEventsByRoundApi, fakeEventSchedulesApi, fakeTournamentInfoApi, fakeLeagueDetailsApi, fakeLeagueSeasonInfoApi, fakeEventStatisticsApi));

        apiWorker = new ApiWorker(strategies, redisRateLimiter);

        matchSyncService = new MatchSyncService(fakeCategoryRepository, fakeTournamentRepository, fakeSeasonRepository, fakeRoundRepository, fakeMatchRepository, fakeStatisticRepository, apiWorker, playerProvider, fixedClock, null, null, null);

    }

    @Test
    public void 이벤트_스케줄_매치_저장(){
        fakeRoundRepository.collect(List.of(RoundFixtures.wimbledonMen2025Final(), RoundFixtures.wimbledonMen2025SemiFinal(), RoundFixtures.rolandGarrosMen2025Final(), RoundFixtures.rolandGarrosMen2025SemiFinal()));
        fakeRoundRepository.flushAll();


        matchSyncService.saveEventSchedule();

        List<Match> saved = fakeMatchRepository.findAll();

        assertThat(saved).hasSize(4);  // 중복 제거 확인
        assertThat(saved)
                .extracting(Match::getRapidMatchId, Match::getStartTimestamp, Match::getWinner, Match::getHomeScore, Match::getAwayScore)
                 .containsExactlyInAnyOrder(
                        tuple("1", "20251011080000", null, 0L, 0L),
                        tuple("2", "20251011100000", null, 0L, 0L),
                        tuple("3", "20251011120000", null, 0L, 0L),
                        tuple("4", "20251011140000", "2", 1L, 3L)
                );
    }

    @Test
    public void 토너먼트_시즌_라운드가_없으면_새로_DB에_저장(){

        matchSyncService.saveEventSchedule();

        Tournament findTournament = fakeTournamentRepository.findByRapidTournamentId(TournamentFixtures.wimbledonATP().getRapidTournamentId()).get();
        Season findSeason = fakeSeasonRepository.findByRapidSeasonId(SeasonFixtures.wimbledonMen2025().getRapidSeasonId()).get();
        Round findRound = fakeRoundRepository.findBySeasonAndRoundAndSlug(findSeason,
                RoundFixtures.wimbledonMen2025QuarterFinal().getRound(),
                RoundFixtures.wimbledonMen2025QuarterFinal().getSlug()).get();

        assertThat(findTournament.getTournamentName()).isEqualTo("Wimbledon");
        assertThat(findSeason.getYear()).isEqualTo("2025");
        assertThat(findSeason.getStartTimestamp()).isEqualTo("20250623090000");
        assertThat(findSeason.getEndTimestamp()).isEqualTo("20250713090000");
        assertThat(findRound.getName()).isEqualTo("Quarterfinals");


    }

    @Test
    public void 토너먼트만_있을때_시즌에_timestamp_저장(){

        fakeTournamentRepository.save(TournamentFixtures.wimbledonATP());
        matchSyncService.saveEventSchedule();

        Tournament findTournament = fakeTournamentRepository.findByRapidTournamentId(TournamentFixtures.wimbledonATP().getRapidTournamentId()).get();
        Season findSeason = fakeSeasonRepository.findByRapidSeasonId(SeasonFixtures.wimbledonMen2025().getRapidSeasonId()).get();
        Round findRound = fakeRoundRepository.findBySeasonAndRoundAndSlug(findSeason,
                RoundFixtures.wimbledonMen2025QuarterFinal().getRound(),
                RoundFixtures.wimbledonMen2025QuarterFinal().getSlug()).get();

        assertThat(findTournament.getTournamentName()).isEqualTo("Wimbledon");
        assertThat(findSeason.getYear()).isEqualTo("2025");
        assertThat(findSeason.getStartTimestamp()).isEqualTo("20250623090000");
        assertThat(findSeason.getEndTimestamp()).isEqualTo("20250713090000");
        assertThat(findRound.getName()).isEqualTo("Quarterfinals");


    }

    @Test
    public void 토너먼트과_시즌이_있을때_시즌에_timestamp_저장(){

        fakeTournamentRepository.save(TournamentFixtures.wimbledonATP());
        fakeSeasonRepository.save(SeasonFixtures.wimbledonMen2025());
        matchSyncService.saveEventSchedule();

        Tournament findTournament = fakeTournamentRepository.findByRapidTournamentId(TournamentFixtures.wimbledonATP().getRapidTournamentId()).get();
        Season findSeason = fakeSeasonRepository.findByRapidSeasonId(SeasonFixtures.wimbledonMen2025().getRapidSeasonId()).get();
        Round findRound = fakeRoundRepository.findBySeasonAndRoundAndSlug(findSeason,
                RoundFixtures.wimbledonMen2025QuarterFinal().getRound(),
                RoundFixtures.wimbledonMen2025QuarterFinal().getSlug()).get();

        assertThat(findTournament.getTournamentName()).isEqualTo("Wimbledon");
        assertThat(findSeason.getYear()).isEqualTo("2025");
        assertThat(findSeason.getStartTimestamp()).isEqualTo("20250623090000");
        assertThat(findSeason.getEndTimestamp()).isEqualTo("20250713090000");
        assertThat(findRound.getName()).isEqualTo("Quarterfinals");


    }


    @Test
    public void 매치가_이미_존재할_때_중복_저장_하지_않음(){
        fakeMatchRepository.save(EventSchedulesFixtures.wimbledonMen2025QuarterFinalsMatch4());

        matchSyncService.saveEventSchedule();

        List<Match> saved = fakeMatchRepository.findAll();

        assertThat(saved).hasSize(4);  // 중복 제거 확인

    }

    @Test
    public void 경기_존재_시_이벤트_등록_시_경기_정보_저장(){
        fakeMatchRepository.saveAll(List.of(EventSchedulesFixtures.wimbledonMen2025QuarterFinalsMatch1(),
                EventSchedulesFixtures.wimbledonMen2025QuarterFinalsMatch2(),
                EventSchedulesFixtures.wimbledonMen2025QuarterFinalsMatch3(),
                EventSchedulesFixtures.wimbledonMen2025QuarterFinalsMatch4()));

        matchSyncService.saveEventSchedule();

        List<Statistic> saved = fakeStatisticRepository.findAll();

        assertThat(saved).hasSize(4);
        assertThat(saved).extracting(Statistic::getStatId, s -> s.getMatch().getMatchId(), Statistic::getPeriod, Statistic::getGroupName,
                        Statistic::getStatDirection, Statistic::getMetric,
                        Statistic::getHomeValue, Statistic::getHomeTotal, Statistic::getAwayValue, Statistic::getAwayTotal)
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, "ALL", "First serve",
                                "positive", "firstServeAccuracy",
                                40L, 60L, 55L, 92L),
                        tuple(2L, 1L, "ALL", "Second serve",
                                "positive", "secondServeAccuracy",
                                19L, 20L, 31L, 37L),
                        tuple(3L, 2L, "1ST", "First serve return points",
                                "positive", "firstReturnPoints",
                                1L, 17L, 8L, 27L),
                        tuple(4L, 1L, "1ST", "Double faults",
                                "negative", "doubleFaults",
                                0L, null, 2L, null)
                );
    }

    @Test
    public void 경기_없을_때_이벤트_등록_시_경기_정보_저장(){

        matchSyncService.saveEventSchedule();

        List<Statistic> saved = fakeStatisticRepository.findAll();

        assertThat(saved).hasSize(4);
        assertThat(saved).extracting(Statistic::getStatId, s -> s.getMatch().getMatchId(), Statistic::getPeriod, Statistic::getGroupName,
                        Statistic::getStatDirection, Statistic::getMetric,
                        Statistic::getHomeValue, Statistic::getHomeTotal, Statistic::getAwayValue, Statistic::getAwayTotal)
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, "ALL", "First serve",
                                "positive", "firstServeAccuracy",
                                40L, 60L, 55L, 92L),
                        tuple(2L, 1L, "ALL", "Second serve",
                                "positive", "secondServeAccuracy",
                                19L, 20L, 31L, 37L),
                        tuple(3L, 2L, "1ST", "First serve return points",
                                "positive", "firstReturnPoints",
                                1L, 17L, 8L, 27L),
                        tuple(4L, 1L, "1ST", "Double faults",
                                "negative", "doubleFaults",
                                0L, null, 2L, null)
                );
    }

}
