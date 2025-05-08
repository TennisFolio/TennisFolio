package com.tennisfolio.Tennisfolio.api.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.api.base.SaveStrategy;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeagueEventsByRoundSaveStrategy implements SaveStrategy<List<Match>> {
    private final MatchRepository matchRepository;

    public LeagueEventsByRoundSaveStrategy(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    public List<Match> save(List<Match> entity) {
        List<Match> toSave = entity.stream().map(match -> {
            matchRepository.findByRapidMatchId(match.getRapidMatchId())
                    .ifPresent(existing -> match.setMatchId(existing.getMatchId()));
            return match;

        }).collect(Collectors.toList());
        return matchRepository.saveAll(toSave);
    }
}
