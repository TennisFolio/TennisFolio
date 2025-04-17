package com.tennisfolio.Tennisfolio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.api.teamdetails.CountryDTO;
import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsResponseParser;
import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ResponseParserTest {

    @Autowired
    TeamDetailsTemplate teamDetailsTemplate;

    @Autowired
    TeamDetailsResponseParser parser;
    //TeamDetails
    @Test
    void api응답저장1() throws Exception{
        String response = teamDetailsTemplate.callApi("250857");

        Files.writeString(Path.of("src/test/resources/teamDetails_250857.json"), response);
    }

    @Test
    void api응답저장2() throws Exception{
        String response = teamDetailsTemplate.callApi("206570");

        Files.writeString(Path.of("src/test/resources/teamDetails_206570.json"), response);
    }

    @Test
    void api응답저장3() throws Exception{
        String response = teamDetailsTemplate.callApi("57163");

        Files.writeString(Path.of("src/test/resources/teamDetails_57163.json"), response);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "teamDetails_57163.json",
        "teamDetails_206570.json",
        "teamDetails_250857.json",
    })
    void 응답파싱테스트(String filename) throws Exception{
        // Given
        String response = Files.readString(Path.of("src/test/resources/"+filename));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response);
        JsonNode teamNode = rootNode.path("team");
        JsonNode playerInfoNode = teamNode.path("playerTeamInfo");


        // Expected
        TeamDetailsApiDTO expected = new TeamDetailsApiDTO();
        expected.setPlayerRapidId(teamNode.path("id").asText(null));
        expected.setPlayerName(teamNode.path("fullName").asText(null));
        expected.setHeight(playerInfoNode.path("height").asText());
        expected.setWeight(playerInfoNode.path("weight").asText());
        expected.setBirthDate(parser.transTimeStamp(playerInfoNode.path("birthDateTimestamp").asText()));
        expected.setPlays(playerInfoNode.path("plays").asText(null));
        expected.setTurnedPro(playerInfoNode.path("turnedPro").asText(null));
        expected.setCountry(new CountryDTO(teamNode.path("country").path("alpha").asText(null),
                teamNode.path("country").path("name").asText(null)));
        expected.setPrizeCurrent(parser.eurToUsd(playerInfoNode.path("prizeCurrentRaw").path("value").asLong(), playerInfoNode.path("prizeCurrentRaw").path("currency").asText(null)));
        expected.setPrizeTotal(parser.eurToUsd(playerInfoNode.path("prizeTotalRaw").path("value").asLong(), playerInfoNode.path("prizeTotalRaw").path("currency").asText(null)));

        // When
        TeamDetailsApiDTO actual = parser.parse(response);

        // Then
        assertThat(actual.getPlayerRapidId()).isEqualTo(expected.getPlayerRapidId());
        assertThat(actual.getPlayerName()).isEqualTo(expected.getPlayerName());
        assertThat(actual.getBirthDate()).isEqualTo(expected.getBirthDate());
        assertThat(actual.getHeight()).isEqualTo(expected.getHeight());
        assertThat(actual.getPlays()).isEqualTo(expected.getPlays());
        assertThat(actual.getTurnedPro()).isEqualTo(expected.getTurnedPro());
        assertThat(actual.getPrizeCurrent()).isEqualTo(expected.getPrizeCurrent());
        assertThat(actual.getPrizeTotal()).isEqualTo(expected.getPrizeTotal());
        assertThat(actual.getCountry().getName()).isEqualTo(expected.getCountry().getName());


    }
}
