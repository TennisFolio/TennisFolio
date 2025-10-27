package com.tennisfolio.Tennisfolio.player.service;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageStorage;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.TeamImageDownloader;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.teamDetails.FakeTeamDetailsEntityMapper;
import com.tennisfolio.Tennisfolio.mock.FakePlayerRepository;
import com.tennisfolio.Tennisfolio.mock.teamDetails.FakeTeamDetailsResponseParser;
import com.tennisfolio.Tennisfolio.mock.teamDetails.FakeTeamDetailsApiTemplate;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class playerServiceTest {
    // 테스트할 모듈
    PlayerService playerService;

    // repository
    FakePlayerRepository fakePlayerRepository = new FakePlayerRepository();

    // api
    ResponseParser<TeamDetailsApiDTO> fakeResponseParser = new FakeTeamDetailsResponseParser();
    EntityMapper<TeamDetailsApiDTO, PlayerAggregate> fakeEntityMapper = new FakeTeamDetailsEntityMapper();
    ApiCaller fakeApiCaller = new FakeApiCaller();

    StrategyApiTemplate<TeamDetailsApiDTO, PlayerAggregate> fakeStrategyApiTemplate = new FakeTeamDetailsApiTemplate(fakeApiCaller, fakeResponseParser, fakeEntityMapper, null, RapidApi.TEAMDETAILS);

    // image 다운로드
    TeamImageDownloader fakeDownloader = mock(TeamImageDownloader.class);
    PlayerImageStorage fakeStorage = mock(PlayerImageStorage.class);


    @BeforeEach
    void setUp() {
        this.playerService = PlayerService.builder()
                .playerImageService(new PlayerImageService(fakeDownloader, fakeStorage))
                .playerRepository(fakePlayerRepository)
                .teamDetailsTemplate(fakeStrategyApiTemplate)
                .build();
    }

    @Test
    void getOrCreatePlayerByRapidId_이미_존재하면_조회된다(){
        Player player = Player.builder()
                .playerId(1L)
                .rapidPlayerId("1")
                .playerName("Alcaraz")
                .build();

        fakePlayerRepository.save(player);

        Player findPlayer = playerService.getOrCreatePlayerByRapidId("1");

        assertThat(player.getPlayerId()).isEqualTo(findPlayer.getPlayerId());
        assertThat(player.getRapidPlayerId()).isEqualTo(findPlayer.getRapidPlayerId());
        assertThat(player.getPlayerName()).isEqualTo(findPlayer.getPlayerName());

    }

    @Test
    void getOrCreatePlayerByRapidId_존재하지_않으면_저장한다(){

        Player player = playerService.getOrCreatePlayerByRapidId("1");

        Player findPlayer = fakePlayerRepository.findByRapidPlayerId("1").get();

        assertThat(player.getPlayerId()).isEqualTo(findPlayer.getPlayerId());
        assertThat(player.getRapidPlayerId()).isEqualTo(findPlayer.getRapidPlayerId());
        assertThat(player.getPlayerName()).isEqualTo(findPlayer.getPlayerName());

    }

}
