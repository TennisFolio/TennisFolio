package com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class EventSchedulesResponseParserTest {
    private EventSchedulesResponseParser parser = new EventSchedulesResponseParser();

    @Test
    void 이벤트_스케줄_DTO_변환_확인(){
        // 리소스 폴더에서 JSON 파일 읽기
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tennisApiSample/EventSchedulesTestData.json");

        String jsonString = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));


        List<EventSchedulesDTO> result = parser.parse(jsonString);

        assertThat(result)
                .extracting(EventSchedulesDTO::getRapidId, EventSchedulesDTO::getStartTimestamp, p-> p.getTournament().getRapidId(),
                        p -> p.getSeason().getRapidId(), p -> p.getRound().getRound())
                .containsExactlyInAnyOrder(
                        tuple("14762084", "1759746600", "2519", "67307", 6L),
                        tuple("14762081", "1759750800", "2519", "67307", 6L),
                        tuple("14762035", "1759811400", "2519", "67307", 5L),
                        tuple("14762037", "1759815600", "2519", "67307", 5L)
                );
    }
}
