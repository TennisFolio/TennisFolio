package com.tennisfolio.Tennisfolio.infrastructure.api.match.event;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules.EventSchedulesDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Period;
import com.tennisfolio.Tennisfolio.match.domain.Score;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventEntityMapper implements EntityMapper<EventDTO, Match> {

    @Override
    public Match map(EventDTO dto, Object... params) {

        return Match.builder()
                .rapidMatchId(dto.getRapidId())
                .homeSeed(dto.getHomeTeamSeed())
                .awaySeed(dto.getAwayTeamSeed())
                .homeScore(dto.getHomeScore().getCurrent())
                .awayScore(dto.getAwayScore().getCurrent())
                .homeSet(Score.from(dto.getHomeScore()))
                .awaySet(Score.from(dto.getAwayScore()))
                .periodSet(Period.from(dto.getTime()))
                .startTimestamp(dto.getStartTimestamp())
                .winner(dto.getWinner())
                .status(dto.getStatus().getDescription())
                .build();

    }
}
