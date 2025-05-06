package com.tennisfolio.Tennisfolio.api.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.api.base.SaveStrategy;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoSaveStrategy implements SaveStrategy<Tournament> {
    private final TournamentRepository tournamentRepository;

    public TournamentInfoSaveStrategy(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public Tournament save(Tournament entity) {
        return tournamentRepository.save(entity);
    }
}
