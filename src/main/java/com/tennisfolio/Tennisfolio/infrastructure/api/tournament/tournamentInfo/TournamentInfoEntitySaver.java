package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.repository.TournamentJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoEntitySaver implements EntitySaver<Tournament> {
    private final TournamentJpaRepository tournamentJpaRepository;

    public TournamentInfoEntitySaver(TournamentJpaRepository tournamentJpaRepository) {
        this.tournamentJpaRepository = tournamentJpaRepository;
    }

    @Override
    public Tournament save(Tournament entity) {
        return tournamentJpaRepository.save(entity);
    }
}
