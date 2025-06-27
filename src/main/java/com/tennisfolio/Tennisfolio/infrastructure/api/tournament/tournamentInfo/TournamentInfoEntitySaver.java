package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoEntitySaver implements EntitySaver<Tournament> {
    private final TournamentRepository tournamentRepository;

    public TournamentInfoEntitySaver(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public Tournament save(Tournament entity) {
        return tournamentRepository.save(entity);
    }
}
