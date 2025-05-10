package com.tennisfolio.Tennisfolio.api.eventStatistics;

import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Statistic;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventsStatisticsAssemble implements EntityAssemble<List<EventsStatisticsDTO>, List<Statistic>> {
    private final MatchRepository matchRepository;

    public EventsStatisticsAssemble(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    public List<Statistic> assemble(List<EventsStatisticsDTO> dto, Object... params) {
        if(params.length == 0 || params[0] == null){
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        List<Statistic> result = new ArrayList<>();
        String matchRapidId = params[0].toString();

        Match match = matchRepository.findByRapidMatchId(matchRapidId).get();

        for(EventsStatisticsDTO event : dto){
            String period = event.getPeriod();

            for(StatisticsGroupDTO group : event.getGroups()){
                for(StatisticsDTO item : group.getStatisticsList()){
                    Statistic statistic = new Statistic(period, item, match);
                    result.add(statistic);
                }
            }
        }

        return result;
    }
}
