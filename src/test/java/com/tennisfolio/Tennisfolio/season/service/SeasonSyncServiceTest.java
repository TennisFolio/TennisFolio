package com.tennisfolio.Tennisfolio.season.service;

import com.tennisfolio.Tennisfolio.Tournament.application.TournamentQueryService;
import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.category.application.CategoryService;
import com.tennisfolio.Tennisfolio.fixtures.SeasonFixtures;
import com.tennisfolio.Tennisfolio.fixtures.TournamentFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo.LeagueSeasonInfoDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons.LeagueSeasonsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments.CategoryTournamentsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakeSeasonRepository;
import com.tennisfolio.Tennisfolio.mock.leagueSeasonInfo.FakeLeagueSeasonInfoApiTemplate;
import com.tennisfolio.Tennisfolio.mock.leagueSeasonInfo.FakeLeagueSeasonInfoMapper;
import com.tennisfolio.Tennisfolio.mock.leagueSeasons.FakeLeagueSeasonsApiTemplate;
import com.tennisfolio.Tennisfolio.mock.leagueSeasons.FakeLeagueSeasonsMapper;
import com.tennisfolio.Tennisfolio.season.application.SeasonSyncService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class SeasonSyncServiceTest {

    private SeasonSyncService seasonSyncService;

    private SeasonRepository fakeSeasonRepository = new FakeSeasonRepository();

    private ApiCaller apiCaller = new FakeApiCaller();

    @Mock
    private ResponseParser parser;

    @Mock
    TournamentQueryService tournamentQueryService;

    private List<StrategyApiTemplate<?, ?>> strategies = new ArrayList<>();

    private StrategyApiTemplate<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonTemplate;

    private StrategyApiTemplate<LeagueSeasonInfoDTO, Season> leagueSeasonInfoTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Tournament rolandGarros = TournamentFixtures.rolandGarrosATP();
        Tournament wimbledon = TournamentFixtures.wimbledonATP();

        when(tournamentQueryService.getAllTournament()).thenReturn(List.of(rolandGarros, wimbledon));

        EntityMapper<List<LeagueSeasonsDTO>, List<Season>> fakeLeagueSeasonsMapper = new FakeLeagueSeasonsMapper();
        leagueSeasonTemplate = new FakeLeagueSeasonsApiTemplate(apiCaller, parser, fakeLeagueSeasonsMapper, RapidApi.LEAGUESEASONS);

        EntityMapper<LeagueSeasonInfoDTO, Season> fakeLeagueSeasonInfoMapper = new FakeLeagueSeasonInfoMapper();
        leagueSeasonInfoTemplate = new FakeLeagueSeasonInfoApiTemplate(apiCaller, parser, fakeLeagueSeasonInfoMapper, RapidApi.LEAGUESEASONINFO);

        strategies.add(leagueSeasonTemplate);
        strategies.add(leagueSeasonInfoTemplate);

        ApiWorker apiWorker = new ApiWorker(strategies);
        seasonSyncService = new SeasonSyncService(apiWorker, tournamentQueryService, fakeSeasonRepository);
    }

    @Test
    void 시즌_리스트_정보_저장_확인(){
        assertThat(fakeSeasonRepository.findAll()).isEmpty();

        seasonSyncService.saveSeasonList();

        List<Season> saved = fakeSeasonRepository.findAll();

        assertThat(saved).isNotEmpty();
        assertThat(saved).anyMatch(t -> t.getSeasonName().equals("Wimbledon Men Singles 2025"));
        assertThat(saved).anyMatch(t -> t.getSeasonName().equals("French Open Men Singles 2025"));
    }

    @Test
    void 시즌_저장_중복시_넘어감(){
        List<Season> seasonList = List.of(SeasonFixtures.rolandGarrosMen2024(), SeasonFixtures.rolandGarrosMen2025());

        fakeSeasonRepository.collect(seasonList);
        fakeSeasonRepository.flushAll();

        seasonSyncService.saveSeasonList();

        List<Season> saved = fakeSeasonRepository.findAll();

        assertThat(saved.size()).isEqualTo(4);

    }

    @Test
    void 시즌_정보_저장_확인(){
        seasonSyncService.saveSeasonList();

        seasonSyncService.saveSeasonInfo();

        List<Season> saved = fakeSeasonRepository.findAll();

        assertThat(saved).hasSize(4);
        assertThat(saved)
                .extracting(Season::getCompetitors, t -> t.getTotalPrize(), t -> t.getTotalPrizeCurrency())
                .containsExactlyInAnyOrder(
                        tuple(128L, 19414000L, "EUR"),
                        tuple(128L, 17942000L, "EUR"),
                        tuple(128L, 20509000L, "EUR"),
                        tuple(128L, 19280000L, "EUR")
                );


    }

}
