package com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.mock.FakeRoundRepository;
import com.tennisfolio.Tennisfolio.mock.FakeSeasonRepository;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.when;

public class LeagueEventsByRoundAssembleTest {

    private LeagueEventsByRoundAssemble leagueEventsByRoundAssemble;
    private RoundRepository fakeRoundRepository = new FakeRoundRepository();
    private SeasonRepository fakeSeasonRepository = new FakeSeasonRepository();
    private LeagueEventsByRoundResponseParser parser = new LeagueEventsByRoundResponseParser();
    @Mock
    private PlayerService playerService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        when(playerService.getOrCreatePlayerByRapidId("90080")).thenReturn(Player.builder().rapidPlayerId("90080").build());
        when(playerService.getOrCreatePlayerByRapidId("122366")).thenReturn(Player.builder().rapidPlayerId("122366").build());
        when(playerService.getOrCreatePlayerByRapidId("14882")).thenReturn(Player.builder().rapidPlayerId("14882").build());
        when(playerService.getOrCreatePlayerByRapidId("138546")).thenReturn(Player.builder().rapidPlayerId("138546").build());

        Tournament fakeTournament = Tournament.builder().rapidTournamentId("2363").build();
        Season fakeSeason = Season.builder().seasonId(1L).rapidSeasonId("48186").build();
        Round fakeRound = Round.builder().roundId(1L).round(28L).slug("semifinals").season(fakeSeason).build();
        fakeSeasonRepository.collect(fakeSeason);
        fakeSeasonRepository.flushAll();

        fakeRoundRepository.collect(fakeRound);
        fakeRoundRepository.flushAll();

        leagueEventsByRoundAssemble = new LeagueEventsByRoundAssemble(fakeRoundRepository, fakeSeasonRepository, playerService);
    }

    @Test
    void DTO_매치_Entity_변환(){
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tennisApiSample/LeagueEventsByRoundTestData.json");


        String jsonString = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));

        List<LeagueEventsByRoundDTO> dto = parser.parse(jsonString);

        List<Match> matchList = leagueEventsByRoundAssemble.assemble(dto, "2363", "48186", "28", "semifinals");

        assertThat(matchList)
                .extracting(Match::getRapidMatchId, p -> p.getRound().getSlug(), p -> p.getHomePlayer().getRapidPlayerId(), p -> p.getAwayPlayer().getRapidPlayerId(),
                        p -> p.getStartTimeStamp())
                .containsExactlyInAnyOrder(
                        tuple("10983951", "semifinals", "90080", "122366", "20230127123000"),
                        tuple("10983950", "semifinals", "14882", "138546", "20230127173000")
                );

    }
}
