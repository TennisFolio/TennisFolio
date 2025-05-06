package com.tennisfolio.Tennisfolio.api.leagueRounds;

import com.tennisfolio.Tennisfolio.api.base.SaveStrategy;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeagueRoundsSaveStrategy implements SaveStrategy<List<Round>> {

    private final RoundRepository roundRepository;

    public LeagueRoundsSaveStrategy(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    @Override
    public List<Round> save(List<Round> entity) {

        List<Round> toSave = entity.stream().map(round -> {
            roundRepository.findBySeasonAndRoundAndSlug(round.getSeason(), round.getRound(), round.getSlug())
                    .ifPresent(existing -> round.setRoundId(existing.getRoundId()));
            return round;
        }).collect(Collectors.toList());

        return roundRepository.saveAll(toSave);
    }
}
