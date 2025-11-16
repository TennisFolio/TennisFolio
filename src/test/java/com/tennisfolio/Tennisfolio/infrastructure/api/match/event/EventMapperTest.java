package com.tennisfolio.Tennisfolio.infrastructure.api.match.event;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class EventMapperTest {
    private EventEntityMapper eventEntityMapper = new EventEntityMapper();
    private EventResponseParser parser = new EventResponseParser();

    @Test
    void 이벤트_변환_테스트() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tennisApiSample/EventTestData.json");

        String jsonString = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));


        EventDTO result = parser.parse(jsonString);

        Match match = eventEntityMapper.map(result);

        assertThat(match.getRapidMatchId()).isEqualTo("14956532");
        assertThat(match.getHomeSeed()).isEqualTo("5");
        assertThat(match.getAwaySeed()).isEqualTo("2");
        assertThat(match.getHomeScore()).isEqualTo(1);
        assertThat(match.getAwayScore()).isEqualTo(1);
        assertThat(match.getStatus()).isEqualTo("3rd set");
        assertThat(match.getPeriodSet().getSet1()).isEqualTo("30:29");
        assertThat(match.getStartTimestamp()).isEqualTo("20251108140000");
        assertThat(match.getWinner()).isNull();
    }
}
