package com.tennisfolio.Tennisfolio.calendar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.calendar.dto.MatchScheduleResponse;
import com.tennisfolio.Tennisfolio.config.IntegrationTest;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiCaller;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules.EventSchedulesDTO;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@IntegrationTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/sql/calendar.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class CalendarIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private StringRedisTemplate redis;

    ObjectMapper objectMapper = new ObjectMapper();

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

    @Test
    void 실시간_경기_중_달력_디테일_조회() throws Exception{
        setRedis();

        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        mockMvc.perform(get("/api/calendar/detail")
                .param("date", today)
                .param("categoryId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].homeScore").value(1))
                .andExpect(jsonPath("$.data[0].awayScore").value(0))
                .andExpect(jsonPath("$.data[0].roundNameKr").value("결승"))
                .andExpect(jsonPath("$.data[0].status").value("2nd set"));

    }

    private void setRedis() throws Exception {
        try{
            LiveMatchResponse liveMatch = loadLiveMatch("redisFixtures/live_atp_15222427.json");

            String redisKey = "index:rapidId:" + liveMatch.getRapidId();

            String json = objectMapper.writeValueAsString(liveMatch);

            redis.opsForValue().set(redisKey, json);
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }
    }


    @Test
    void 실시간_경기_중_달력_디테일_레디스_DB_조회() throws Exception{
        setRedis();

        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        mockMvc.perform(get("/api/calendar/detail")
                        .param("date", today)
                        .param("categoryId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].homeScore").value(1))
                .andExpect(jsonPath("$.data[0].awayScore").value(0))
                .andExpect(jsonPath("$.data[0].roundNameKr").value("결승"))
                .andExpect(jsonPath("$.data[0].status").value("2nd set"))
                .andExpect(jsonPath("$.data[0].live").value(true))
                .andExpect(jsonPath("$.data[1].homeScore").value(0))
                .andExpect(jsonPath("$.data[1].awayScore").value(0))
                .andExpect(jsonPath("$.data[1].roundNameKr").value("결승"))
                .andExpect(jsonPath("$.data[1].status").value("Not started"))
                .andExpect(jsonPath("$.data[1].live").value(false));

    }

    private LiveMatchResponse loadLiveMatch(String fileName) throws Exception {
        ClassPathResource resource = new ClassPathResource(fileName);
        return objectMapper.readValue(
                resource.getInputStream(),
                LiveMatchResponse.class
        );
    }

}
