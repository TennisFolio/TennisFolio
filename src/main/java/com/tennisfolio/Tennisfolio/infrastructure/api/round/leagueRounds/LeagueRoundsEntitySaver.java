package com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeagueRoundsEntitySaver implements EntitySaver<List<Round>> {

    private final RoundRepository roundRepository;

    public LeagueRoundsEntitySaver(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    @Override
    public List<Round> save(List<Round> entity) {

        List<Round> toSave = entity.stream()
                .filter(round -> roundRepository.findBySeasonAndRoundAndSlug(round.getSeason(), round.getRound(), round.getSlug()).isEmpty())
                .collect(Collectors.toList());

        return roundRepository.saveAll(toSave);
    }
}
