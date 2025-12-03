package com.tennisfolio.Tennisfolio.prize.application;

import com.tennisfolio.Tennisfolio.fixtures.PlayerFixtures;
import com.tennisfolio.Tennisfolio.fixtures.PlayerPrizeFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamDetails.TeamDetailsTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakePlayerRepository;
import com.tennisfolio.Tennisfolio.mock.FakePrizeRepository;
import com.tennisfolio.Tennisfolio.mock.teamDetails.FakeTeamDetailsApiTemplate;
import com.tennisfolio.Tennisfolio.mock.teamDetails.FakeTeamDetailsEntityMapper;
import com.tennisfolio.Tennisfolio.mock.teamDetails.FakeTeamDetailsResponseParser;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import com.tennisfolio.Tennisfolio.prize.repository.PrizeRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class PrizeSyncServiceTest {

    PrizeSyncService prizeSyncService;

    PlayerRepository fakePlayerRepository = new FakePlayerRepository();
    PrizeRepository fakePrizeRepository = new FakePrizeRepository();

    @Mock
    private ApiCallCounter apiCallCounter;

    @Mock
    private RedisRateLimiter redisRateLimiter;

    ResponseParser<TeamDetailsApiDTO> fakeResponseParser = new FakeTeamDetailsResponseParser();
    EntityMapper<TeamDetailsApiDTO, PlayerAggregate> fakeEntityMapper = new FakeTeamDetailsEntityMapper();
    ApiCaller fakeApiCaller = new FakeApiCaller();
    ApiWorker apiWorker;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        List<StrategyApiTemplate<?, ?>> strategies = new ArrayList<>();

        StrategyApiTemplate<TeamDetailsApiDTO, PlayerAggregate> teamDetailsTemplate = new FakeTeamDetailsApiTemplate(fakeApiCaller, fakeResponseParser, fakeEntityMapper, apiCallCounter,RapidApi.TEAMDETAILS);

        strategies.add(teamDetailsTemplate);

        apiWorker = new ApiWorker(strategies, redisRateLimiter);

        this.prizeSyncService = PrizeSyncService.builder()
                .apiWorker(apiWorker)
                .prizeRepository(fakePrizeRepository)
                .playerRepository(fakePlayerRepository)
                .build();
    }

    @Test
    void 데이터_없을_때_모두_저장_확인(){

        fakePlayerRepository.saveAll(List.of(PlayerFixtures.nadal(), PlayerFixtures.alcaraz(), PlayerFixtures.sinner()));

        prizeSyncService.savePlayerPrize();

        List<PlayerPrize> saved = fakePrizeRepository.findAll();

        assertThat(saved).hasSize(3);
        assertThat(saved)
                .extracting(pp -> pp.getPlayer().getRapidPlayerId(), pp -> pp.getPrizeCurrentAmount(),
                        PlayerPrize::getPrizeCurrentCurrency, PlayerPrize::getPrizeTotalAmount, PlayerPrize::getPrizeTotalCurrency)
                .containsExactlyInAnyOrder(
                        tuple("14486", 20L, "EUR", 400L, "EUR"),
                        tuple("275923", 100L, "EUR", 200L, "EUR"),
                        tuple("206570", 80L, "EUR", 240L, "EUR")
                );

    }

    @Test
    void 데이터_있을_때_수정_확인(){

        fakePlayerRepository.saveAll(List.of(PlayerFixtures.nadal(), PlayerFixtures.alcaraz(), PlayerFixtures.sinner()));
        fakePrizeRepository.save(PlayerPrizeFixtures.nadal());
        fakePrizeRepository.save(PlayerPrizeFixtures.alcaraz());
        fakePrizeRepository.save(PlayerPrizeFixtures.sinner());

        prizeSyncService.savePlayerPrize();

        List<PlayerPrize> saved = fakePrizeRepository.findAll();

        assertThat(saved).hasSize(3);
        assertThat(saved)
                .extracting(pp -> pp.getPlayer().getRapidPlayerId(), pp -> pp.getPrizeCurrentAmount(),
                        PlayerPrize::getPrizeCurrentCurrency, PlayerPrize::getPrizeTotalAmount, PlayerPrize::getPrizeTotalCurrency)
                .containsExactlyInAnyOrder(
                        tuple("14486", 20L, "EUR", 400L, "EUR"),
                        tuple("275923", 100L, "EUR", 200L, "EUR"),
                        tuple("206570", 80L, "EUR", 240L, "EUR")
                );

    }
}

