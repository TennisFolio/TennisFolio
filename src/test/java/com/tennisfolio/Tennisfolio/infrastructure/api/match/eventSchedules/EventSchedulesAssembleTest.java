package com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules;

import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.mock.FakeCategoryRepository;
import com.tennisfolio.Tennisfolio.mock.FakeRoundRepository;
import com.tennisfolio.Tennisfolio.mock.FakeSeasonRepository;
import com.tennisfolio.Tennisfolio.mock.FakeTournamentRepository;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class EventSchedulesAssembleTest {
    private EventSchedulesAssemble assembler;
    private CategoryRepository fakeCategoryRepository = new FakeCategoryRepository();
    private TournamentRepository fakeTournamentRepository = new FakeTournamentRepository();
    private SeasonRepository fakeSeasonRepository = new FakeSeasonRepository();
    private RoundRepository fakeRoundRepository = new FakeRoundRepository();
    private EventSchedulesResponseParser parser = new EventSchedulesResponseParser();

    @BeforeEach
    public void init(){
        assembler = new EventSchedulesAssemble(fakeCategoryRepository, fakeTournamentRepository, fakeSeasonRepository, fakeRoundRepository);
    }

    @Test
    void 이벤트_스케줄_변환_테스트() {
        // 리소스 폴더에서 JSON 파일 읽기
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tennisApiSample/EventSchedulesTestData.json");

        String jsonString = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));

        List<EventSchedulesDTO> result = parser.parse(jsonString);

        List<Match> matchList = assembler.assemble(result, "6", "10", "2025");

        assertThat(matchList)
                .extracting(Match::getRapidMatchId, p -> p.getRound().getRound(), p -> p.getHomePlayer().getRapidPlayerId(), p -> p.getAwayPlayer().getRapidPlayerId(),
                        p -> p.getStartTimeStamp())
                .containsExactlyInAnyOrder(
                        tuple("14762084", 6L, "57163", "63606", "20251006193000"),
                        tuple("14762081", 6L, "157456", "163504", "20251006204000"),
                        tuple("14762035", 5L, "280151", "170946", "20251007133000"),
                        tuple("14762037", 5L, "289146", "283070", "20251007144000")
                );

    }
}
