package com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeagueEventsByRoundEntitySaver implements EntitySaver<List<Match>> {
    private final MatchRepository matchRepository;

    public LeagueEventsByRoundEntitySaver(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    public List<Match> save(List<Match> entity) {
        List<Match> toSave = entity.stream()
                .filter(match -> matchRepository.findByRapidMatchId(match.getRapidMatchId()).isEmpty())
                .collect(Collectors.toList());

        return matchRepository.saveAll(toSave);
    }
}
