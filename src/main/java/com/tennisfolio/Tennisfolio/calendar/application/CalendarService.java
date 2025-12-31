package com.tennisfolio.Tennisfolio.calendar.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentQueryRepository;
import com.tennisfolio.Tennisfolio.calendar.dto.MatchScheduleResponse;
import com.tennisfolio.Tennisfolio.calendar.dto.TournamentCalendarResponse;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.match.repository.MatchQueryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CalendarService {

    private final TournamentQueryRepository tournamentQueryRepository;
    private final MatchQueryRepository matchQueryRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CalendarService(TournamentQueryRepository tournamentQueryRepository, MatchQueryRepository matchQueryRepository, StringRedisTemplate redisTemplate) {
        this.tournamentQueryRepository = tournamentQueryRepository;
        this.matchQueryRepository = matchQueryRepository;
        this.redisTemplate = redisTemplate;
     }

    public List<TournamentCalendarResponse> getTournamentCalendar(String month){

        return tournamentQueryRepository.findTournamentCalendar(month);
    }

    public List<MatchScheduleResponse> getMatchSchedule(String date, Long seasonId, Long categoryId){
        List<MatchScheduleResponse> schedules =
                matchQueryRepository.findMatchSchedule(date, seasonId, categoryId);

        if(!isToady(date)){
            return schedules;
        }

        applyLiveMatches(schedules);

        return schedules;
    }

    private boolean isToady(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate requestDate = LocalDate.parse(date, formatter);
        return requestDate.equals(LocalDate.now());
    }

    private LiveMatchResponse findLiveMatch(String rapidMatchId){
        try{
            String json = redisTemplate.opsForValue().get(liveMatchKey(rapidMatchId));
            if(json == null) return null;
            return objectMapper.readValue(json, LiveMatchResponse.class);
        }catch(JsonProcessingException e){
            log.error("Json Parsing Error : " + e);
        }

        return null;

    }

    private String liveMatchKey(String rapidMatchId){
        return "index:rapidId:" + rapidMatchId;
    }

    private void applyLiveMatches(List<MatchScheduleResponse> schedules){
        for (MatchScheduleResponse schedule : schedules){
            LiveMatchResponse liveMatch =
                    findLiveMatch(schedule.getRapidMatchId());

            if(liveMatch != null){
                schedule.applyLiveMatch(liveMatch);
            }
        }
    }
}
