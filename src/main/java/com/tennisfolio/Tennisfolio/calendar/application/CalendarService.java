package com.tennisfolio.Tennisfolio.calendar.application;

import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentQueryRepository;
import com.tennisfolio.Tennisfolio.calendar.dto.MatchScheduleResponse;
import com.tennisfolio.Tennisfolio.calendar.dto.TournamentCalendarResponse;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.match.repository.MatchQueryRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final TournamentQueryRepository tournamentQueryRepository;
    private final MatchQueryRepository matchQueryRepository;
    private final RedisTemplate redisTemplate;

    public CalendarService(TournamentQueryRepository tournamentQueryRepository, MatchQueryRepository matchQueryRepository, @Qualifier("redisTemplate") RedisTemplate redisTemplate) {
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
        return (LiveMatchResponse) redisTemplate.opsForValue().get(liveMatchKey(rapidMatchId));
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
