package com.tennisfolio.Tennisfolio.player.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/sql/playerDetail.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class PlayerDetailIntegrationTest {
    @Autowired
    private MockMvc mockMvc;



    @Test
    void 선수_개인_데이터_조회_성공() throws Exception{
        mockMvc.perform(get("/api/player/{playerId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.playerId").value(1))
                .andExpect(jsonPath("$.data.rapidPlayerId").value("275923"))
                .andExpect(jsonPath("$.data.playerName").value("Alcaraz"))
                .andExpect(jsonPath("$.data.playerNameKr").value("알카라즈"))
                .andExpect(jsonPath("$.data.birth").value("20030505"))
                .andExpect(jsonPath("$.data.countryCode").value("ES"))
                .andExpect(jsonPath("$.data.turnedPro").value("2018"))
                .andExpect(jsonPath("$.data.weight").value("74"))
                .andExpect(jsonPath("$.data.plays").value("right-handed"))
                .andExpect(jsonPath("$.data.height").value("183"))
                .andExpect(jsonPath("$.data.image").value("player/275923"))
                .andExpect(jsonPath("$.data.prizeCurrentAmount").value(100))
                .andExpect(jsonPath("$.data.prizeCurrentCurrency").value("EUR"))
                .andExpect(jsonPath("$.data.prizeTotalAmount").value(500))
                .andExpect(jsonPath("$.data.prizeTotalCurrency").value("EUR"))
                .andExpect(jsonPath("$.data.rankingId").value(2))
                .andExpect(jsonPath("$.data.curRanking").value(1))
                .andExpect(jsonPath("$.data.curPoints").value(10000))
                .andExpect(jsonPath("$.data.bestRank").value(1));

    }

    @Test
    void 선수_개인_데이터_조회_실패_존재하지_않음() throws Exception{
        mockMvc.perform(get("/api/player/{playerId}", 999))
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    void 선수_매치_데이터_조회_성공() throws Exception{
        mockMvc.perform(get("/api/player/{playerId}/match", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].categoryId").value(1))
                .andExpect(jsonPath("$.data[0].categoryName").value("ATP"))
                .andExpect(jsonPath("$.data[0].tournamentId").value(2))
                .andExpect(jsonPath("$.data[0].tournamentName").value("Dubai"))
                .andExpect(jsonPath("$.data[0].seasonId").value(2))
                .andExpect(jsonPath("$.data[0].seasonName").value("ATP Dubai, UAE Men Singles 2025"))
                .andExpect(jsonPath("$.data[0].roundId").value(5))
                .andExpect(jsonPath("$.data[0].roundName").value("Semifinals"))
                .andExpect(jsonPath("$.data[0].roundNameKr").value("준결승"))
                .andExpect(jsonPath("$.data[0].matchId").value(11))
                .andExpect(jsonPath("$.data[0].rapidMatchId").value("11"))
                .andExpect(jsonPath("$.data[0].homePlayerId").value(1))
                .andExpect(jsonPath("$.data[0].homePlayerName").value("Alcaraz"))
                .andExpect(jsonPath("$.data[0].homePlayerNameKr").value("알카라즈"))
                .andExpect(jsonPath("$.data[0].homePlayerSet[0]").value(6))
                .andExpect(jsonPath("$.data[0].homePlayerSetTie[0]").value(3))
                .andExpect(jsonPath("$.data[0].awayPlayerId").value(6))
                .andExpect(jsonPath("$.data[0].awayPlayerName").value("Thompson"))
                .andExpect(jsonPath("$.data[0].awayPlayerNameKr").value("톰슨"))
                .andExpect(jsonPath("$.data[0].awayPlayerSet", contains(7,5,2,0,0)))
                .andExpect(jsonPath("$.data[0].awayPlayerSetTie", contains(7,0,0,0,0)))
                .andExpect(jsonPath("$.data[0].winner").value("2"))
                .andExpect(jsonPath("$.data[0].startTimestamp").value("20251105230000"));
    }

    @Test
    void 선수_매치_데이터_조회_실패_존재하지_않음() throws Exception{
        mockMvc.perform(get("/api/player/{playerId}/match", 999))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
