package com.tennisfolio.Tennisfolio.matching.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompetitionCreateRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesWithJsonCreatorConstructor() throws Exception {
        String json = """
                {
                  "mode": "CLUB_SESSION",
                  "competitionName": "Club",
                  "maleCount": 3,
                  "femaleCount": 2,
                  "courtCount": 1,
                  "hours": 1,
                  "seed": 136,
                  "malePlayerNames": ["M1", "민수", "M3"],
                  "femalePlayerNames": ["F1", "지연"]
                }
                """;

        CompetitionCreateRequest request = objectMapper.readValue(json, CompetitionCreateRequest.class);

        assertEquals("CLUB_SESSION", request.getMode());
        assertEquals("Club", request.getCompetitionName());
        assertEquals(3, request.getMaleCount());
        assertEquals(2, request.getFemaleCount());
        assertEquals(1, request.getCourtCount());
        assertEquals(1, request.getHours());
        assertEquals(136L, request.getSeed());
        assertEquals("민수", request.getMalePlayerNames().get(1));
        assertEquals("지연", request.getFemalePlayerNames().get(1));
    }
}
