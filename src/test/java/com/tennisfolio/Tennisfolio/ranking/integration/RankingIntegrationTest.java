package com.tennisfolio.Tennisfolio.ranking.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/sql/ranking.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class RankingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 영어_이름으로_선수_랭킹_검색() throws Exception{
        mockMvc.perform(get("/api/ranking")
                .param("page", "0")
                .param("size", "10")
                .param("category", "ATP")
                .param("name", "i")
                .param("country", "IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].rankingId").value(2L))
                .andExpect(jsonPath("$.data[0].curRanking").value(2L))
                .andExpect(jsonPath("$.data[0].player.playerNameKr").value("시너"))
                .andExpect(jsonPath("$.data[0].preRanking").value(2L))
                .andExpect(jsonPath("$.data[0].bestRanking").value(1L))
                .andExpect(jsonPath("$.data[0].curPoints").value(11500L))
                .andExpect(jsonPath("$.data[0].prePoints").value(11500L))
                .andExpect(jsonPath("$.data[0].gapRanking").value(0L))
                .andExpect(jsonPath("$.data[0].gapPoints").value(0L))
                .andExpect(jsonPath("$.data[0].rankingLastUpdated").value("20251201"));
    }

    @Test
    void 한국_이름으로_선수_랭킹_검색() throws Exception{
        mockMvc.perform(get("/api/ranking")
                        .param("page", "0")
                        .param("size", "10")
                        .param("category", "ATP")
                        .param("name", "알"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].rankingId").value(1L))
                .andExpect(jsonPath("$.data[0].curRanking").value(1L))
                .andExpect(jsonPath("$.data[0].player.playerNameKr").value("알카라즈"))
                .andExpect(jsonPath("$.data[0].preRanking").value(1L))
                .andExpect(jsonPath("$.data[0].bestRanking").value(1L))
                .andExpect(jsonPath("$.data[0].curPoints").value(12050L))
                .andExpect(jsonPath("$.data[0].prePoints").value(12050L))
                .andExpect(jsonPath("$.data[0].gapRanking").value(0L))
                .andExpect(jsonPath("$.data[0].gapPoints").value(0L))
                .andExpect(jsonPath("$.data[0].rankingLastUpdated").value("20251201"));
    }

    @Test
    void 국가로_선수_랭킹_검색() throws Exception{
        mockMvc.perform(get("/api/ranking")
                        .param("page", "0")
                        .param("size", "10")
                        .param("category", "ATP")
                        .param("country", "IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].rankingId").value(2L))
                .andExpect(jsonPath("$.data[0].curRanking").value(2L))
                .andExpect(jsonPath("$.data[0].player.playerNameKr").value("시너"))
                .andExpect(jsonPath("$.data[0].preRanking").value(2L))
                .andExpect(jsonPath("$.data[0].bestRanking").value(1L))
                .andExpect(jsonPath("$.data[0].curPoints").value(11500L))
                .andExpect(jsonPath("$.data[0].prePoints").value(11500L))
                .andExpect(jsonPath("$.data[0].gapRanking").value(0L))
                .andExpect(jsonPath("$.data[0].gapPoints").value(0L))
                .andExpect(jsonPath("$.data[0].rankingLastUpdated").value("20251201"));
    }

    @Test
    void 랭킹_국가_전체_검색() throws Exception{
        mockMvc.perform(get("/api/ranking/country"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[0].countryCode").value( "IT"))
                .andExpect(jsonPath("$.data[0].countryName").value( "Italy"))
                .andExpect(jsonPath("$.data[1].countryCode").value( "RS"))
                .andExpect(jsonPath("$.data[1].countryName").value( "Serbia"))
                .andExpect(jsonPath("$.data[2].countryCode").value( "ES"))
                .andExpect(jsonPath("$.data[2].countryName").value( "Spain"))
                .andExpect(jsonPath("$.data[3].countryCode").value( "US"))
                .andExpect(jsonPath("$.data[3].countryName").value( "USA"));
    }
}
