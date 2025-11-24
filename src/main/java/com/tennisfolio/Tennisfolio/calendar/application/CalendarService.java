package com.tennisfolio.Tennisfolio.calendar.application;

import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentQueryRepository;
import com.tennisfolio.Tennisfolio.calendar.dto.MatchScheduleResponse;
import com.tennisfolio.Tennisfolio.calendar.dto.TournamentCalendarResponse;
import com.tennisfolio.Tennisfolio.match.repository.MatchQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarService {

    private final TournamentQueryRepository tournamentQueryRepository;
    private final MatchQueryRepository matchQueryRepository;

    public CalendarService(TournamentQueryRepository tournamentQueryRepository, MatchQueryRepository matchQueryRepository) {
        this.tournamentQueryRepository = tournamentQueryRepository;
        this.matchQueryRepository = matchQueryRepository;
    }

    public List<TournamentCalendarResponse> getTournamentCalendar(String month){

        return tournamentQueryRepository.findTournamentCalendar(month);
    }

    public List<MatchScheduleResponse> getMatchSchedule(String date, Long seasonId, Long categoryId){
        return matchQueryRepository.findMatchSchedule(date, seasonId, categoryId);
    }
}
