package com.tennisfolio.Tennisfolio.infrastructure.api.match.event;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules.EventSchedulesDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules.EventSchedulesResponseParser;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class EventResponseParserTest {
    private EventResponseParser parser = new EventResponseParser();

    @Test
    void 이벤트_DTO_변환_확인() {
        // 리소스 폴더에서 JSON 파일 읽기
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tennisApiSample/EventTestData.json");

        String jsonString = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));


        EventDTO result = parser.parse(jsonString);

        assertThat(result.getRapidId()).isEqualTo("14956532");
        assertThat(result.getHomeTeamSeed()).isEqualTo("5");
        assertThat(result.getAwayTeamSeed()).isEqualTo("2");
        assertThat(result.getTournament().getRapidId()).isEqualTo("3181");
        assertThat(result.getSeason().getRapidId()).isEqualTo("80576");
        assertThat(result.getRound().getRound()).isEqualTo(28);
        assertThat(result.getHomeTeam().getRapidPlayerId()).isEqualTo("339452");
        assertThat(result.getAwayTeam().getRapidPlayerId()).isEqualTo("59281");
        assertThat(result.getHomeScore().getCurrent()).isEqualTo(1);
        assertThat(result.getAwayScore().getCurrent()).isEqualTo(1);
        assertThat(result.getStatus().getCode()).isEqualTo(10);
        assertThat(result.getTime().getPeriod1()).isEqualTo("1829");
        assertThat(result.getStartTimestamp()).isEqualTo("1762578000");
        assertThat(result.getWinner()).isNull();




    }
}
