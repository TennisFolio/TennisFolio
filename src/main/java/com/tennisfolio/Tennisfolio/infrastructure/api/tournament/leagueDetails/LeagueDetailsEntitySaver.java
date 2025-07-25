package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import org.springframework.stereotype.Component;

@Component
public class LeagueDetailsEntitySaver implements EntitySaver<Tournament> {
    private final TournamentRepository tournamentRepository;

    public LeagueDetailsEntitySaver(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public Tournament save(Tournament entity) {
        return tournamentRepository.save(entity);
    }
}
