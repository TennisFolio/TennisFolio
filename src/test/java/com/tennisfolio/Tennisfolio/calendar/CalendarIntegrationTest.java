package com.tennisfolio.Tennisfolio.calendar;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiCaller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/sql/calendar.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CalendarIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiCaller apiCaller;

    @Test
    void 달력_데이터_조회_성공() throws Exception{
        mockMvc.perform(get("/api/calendar")
                        .param("month", "202510"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tournamentName").value("Australian Open"))
                .andExpect(jsonPath("$.data[1].tournamentName").value("Dubai"));

    }

    @Test
    void 달력_데이터_조회_실패() throws Exception{
        mockMvc.perform(get("/api/calendar")
                .param("month", "2025"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 달력_디테일_매치_조회_성공() throws Exception{
        mockMvc.perform(get("/api/calendar/detail")
                        .param("date", "20251020")
                        .param("seasonId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].homePlayerName").value("Alcaraz"))
                .andExpect(jsonPath("$.data[0].status").value("Ended"))
                .andExpect(jsonPath("$.data[0].roundSlug").value("final"));
    }

    @Test
    void 달력_디테일_매치_시즌_없이_조회_성공() throws Exception{
        mockMvc.perform(get("/api/calendar/detail")
                        .param("date", "20251020"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].homePlayerName").value("Alcaraz"))
                .andExpect(jsonPath("$.data[0].status").value("Ended"))
                .andExpect(jsonPath("$.data[0].roundSlug").value("final"));
    }

    @Test
    void 달력_디테일_카테고리_포함_조회_성공() throws Exception{
        mockMvc.perform(get("/api/calendar/detail")
                .param("date", "20251020")
                .param("categoryId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].homePlayerName").value("Alcaraz"))
                .andExpect(jsonPath("$.data[0].status").value("Ended"))
                .andExpect(jsonPath("$.data[0].roundSlug").value("final"));

    }
    

}
