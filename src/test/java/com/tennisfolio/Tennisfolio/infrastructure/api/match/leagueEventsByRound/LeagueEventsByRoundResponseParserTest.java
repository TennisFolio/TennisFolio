package com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class LeagueEventsByRoundResponseParserTest {
    ObjectMapper objectMapper = new ObjectMapper();
    LeagueEventsByRoundResponseParser parser = new LeagueEventsByRoundResponseParser();

    @Test
    void 매치_데이터_변환_확인(){

        // 리소스 폴더에서 JSON 파일 읽기
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tennisApiSample/LeagueEventsByRoundTestData.json");


        String jsonString = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));


        List<LeagueEventsByRoundDTO> dto = parser.parse(jsonString);

        assertThat(dto)
                .extracting(LeagueEventsByRoundDTO::getRapidMatchId, LeagueEventsByRoundDTO::getStartTimestamp, p-> p.getTournament().getSlug(),
                        p -> p.getSeason().getRapidId(), p -> p.getRound().getSlug())
                .containsExactlyInAnyOrder(
                        tuple("10983951", "1674790200", "australian-open-melbourne-australia", "48186", "semifinals"),
                        tuple("10983950", "1674808200", "australian-open-melbourne-australia", "48186", "semifinals")
                );
    }
}
