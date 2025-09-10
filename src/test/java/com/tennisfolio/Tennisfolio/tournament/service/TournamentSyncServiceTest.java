package com.tennisfolio.Tennisfolio.tournament.service;

import com.tennisfolio.Tennisfolio.Tournament.application.TournamentSyncService;
import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.application.CategoryService;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.fixtures.TournamentFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments.CategoryTournamentsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails.LeagueDetailsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakeTournamentRepository;
import com.tennisfolio.Tennisfolio.mock.atpRanking.FakeAtpRankingEntityMapper;
import com.tennisfolio.Tennisfolio.mock.atpRanking.FakeAtpRankingResponseParser;
import com.tennisfolio.Tennisfolio.mock.categoryTournaments.FakeCategoryTournamentMapper;
import com.tennisfolio.Tennisfolio.mock.categoryTournaments.FakeCategoryTournamentsApiTemplate;
import com.tennisfolio.Tennisfolio.mock.leagueDetails.FakeLeagueDetails;
import com.tennisfolio.Tennisfolio.mock.leagueDetails.FakeLeagueDetailsMapper;
import com.tennisfolio.Tennisfolio.mock.tournamentInfo.FakeTournamentInfo;
import com.tennisfolio.Tennisfolio.mock.tournamentInfo.FakeTournamentInfoMapper;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TournamentSyncServiceTest {
    private TournamentSyncService service;


    ApiCaller apiCaller = new FakeApiCaller();

    @Mock
    CategoryService categoryService;

    @Mock
    ResponseParser<List<CategoryTournamentsDTO>> categoryParser;

    @Mock
    ResponseParser<TournamentInfoDTO> tournamentInfoParser;

    @Mock
    ResponseParser<LeagueDetailsDTO> leagueParser;

    private StrategyApiTemplate<List<CategoryTournamentsDTO>, List<Tournament>> categoryTemplate;
    private StrategyApiTemplate<TournamentInfoDTO, Tournament> infoTemplate;
    private StrategyApiTemplate<LeagueDetailsDTO, Tournament> leagueTemplate;

    private EntityMapper<List<CategoryTournamentsDTO>, List<Tournament>> fakeCategoryTournamentMapper = new FakeCategoryTournamentMapper();
    private EntityMapper<TournamentInfoDTO, Tournament> fakeTournamentInfoMapper = new FakeTournamentInfoMapper();
    private EntityMapper<LeagueDetailsDTO, Tournament> fakeLeagueDetailsMapper = new FakeLeagueDetailsMapper();

    private TournamentRepository fakeTournamentRepository = new FakeTournamentRepository();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // stub 파서 응답
        when(categoryParser.parse(any())).thenReturn(List.of(new CategoryTournamentsDTO()));
        when(tournamentInfoParser.parse(any())).thenReturn(new TournamentInfoDTO());
        when(leagueParser.parse(any())).thenReturn(new LeagueDetailsDTO());
        when(categoryService.getByRapidCategoryIdNotIn(any())).thenReturn(List.of(Category.builder().categoryId(1L).rapidCategoryId("3").build()));
        categoryTemplate = new FakeCategoryTournamentsApiTemplate(apiCaller, categoryParser, fakeCategoryTournamentMapper, RapidApi.CATEGORYTOURNAMENTS);
        infoTemplate = new FakeTournamentInfo(apiCaller, tournamentInfoParser, fakeTournamentInfoMapper, RapidApi.TOURNAMENTINFO);
        leagueTemplate = new FakeLeagueDetails(apiCaller, leagueParser, fakeLeagueDetailsMapper, RapidApi.LEAGUEDETAILS);

        service = new TournamentSyncService(categoryTemplate, infoTemplate, leagueTemplate, categoryService, fakeTournamentRepository);
    }

    @Test
    public void 토너먼트_리스트_저장(){
        // given
        assertThat(fakeTournamentRepository.findAll()).isEmpty();

        // when
        service.saveTournamentList();

        // then
        List<Tournament> saved = fakeTournamentRepository.findAll();
        assertThat(saved).isNotEmpty();
        assertThat(saved).anyMatch(t -> t.getTournamentName().equals("Roland Garros"));
        assertThat(saved).anyMatch(t -> t.getTournamentName().equals("Wimbledon"));
    }

    @Test
    void TournamentInfo_저장() {
        // given
        fakeTournamentRepository.collect(TournamentFixtures.rolandGarrosATP()); // info 필요 상태
        fakeTournamentRepository.flushAll();

        // when
        service.saveTournamentInfo();

        // then
        List<Tournament> saved = fakeTournamentRepository.findAll();
        assertThat(saved).anyMatch(t -> "singles".equals(t.getMatchType()));
    }

    @Test
    void LeagueDetails_저장() {
        // given
        fakeTournamentRepository.collect(TournamentFixtures.rolandGarrosATP()); // league detail 필요 상태
        fakeTournamentRepository.flushAll();

        // when
        service.saveLeagueDetails();

        // then
        List<Tournament> saved = fakeTournamentRepository.findAllWithPlayers();
        assertThat(saved).anyMatch(t -> t.getPoints() != null && t.getPoints() == 2000L);
        assertThat(saved).anyMatch(t -> t.getMostTitlePlayer().getPlayerName().equals("Rafael Nadal"));
    }

}
