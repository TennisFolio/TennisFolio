package com.tennisfolio.Tennisfolio.matching.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                  "totalGames": 10,
                  "seed": 136,
                  "malePlayerNames": ["M1", "M2", "M3"],
                  "femalePlayerNames": ["F1", "F2"]
                }
                """;

        CompetitionCreateRequest request = objectMapper.readValue(json, CompetitionCreateRequest.class);

        assertEquals("CLUB_SESSION", request.getMode());
        assertEquals("Club", request.getCompetitionName());
        assertEquals(3, request.getMaleCount());
        assertEquals(2, request.getFemaleCount());
        assertEquals(1, request.getCourtCount());
        assertEquals(10, request.getTotalGames());
        assertEquals(136L, request.getSeed());
        assertEquals("M2", request.getMalePlayerNames().get(1));
        assertEquals("F2", request.getFemalePlayerNames().get(1));
    }

    @Test
    void deserializesSameGenderDoublesOnly() throws Exception {
        String json = """
                {
                  "mode": "FIXED_SCHEDULE",
                  "competitionName": "Fixed",
                  "maleCount": 8,
                  "femaleCount": 8,
                  "courtCount": 2,
                  "totalGames": 12,
                  "seed": 136,
                  "sameGenderDoublesOnly": true,
                  "malePlayerNames": ["M1", "M2"],
                  "femalePlayerNames": ["F1", "F2"]
                }
                """;

        CompetitionCreateRequest request = objectMapper.readValue(json, CompetitionCreateRequest.class);

        assertTrue(request.isSameGenderDoublesOnly());
    }

    @Test
    void defaultsSameGenderDoublesOnlyToFalseWhenMissing() throws Exception {
        String json = """
                {
                  "mode": "FIXED_SCHEDULE",
                  "competitionName": "Fixed",
                  "maleCount": 8,
                  "femaleCount": 8,
                  "courtCount": 2,
                  "totalGames": 12,
                  "seed": 136,
                  "malePlayerNames": ["M1", "M2"],
                  "femalePlayerNames": ["F1", "F2"]
                }
                """;

        CompetitionCreateRequest request = objectMapper.readValue(json, CompetitionCreateRequest.class);

        assertFalse(request.isSameGenderDoublesOnly());
    }
}