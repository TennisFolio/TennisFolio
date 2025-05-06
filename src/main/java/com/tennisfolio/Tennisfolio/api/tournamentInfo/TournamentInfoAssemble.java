package com.tennisfolio.Tennisfolio.api.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoAssemble implements EntityAssemble<TournamentInfoDTO, Tournament> {
    private final TournamentRepository tournamentRepository;

    public TournamentInfoAssemble(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public Tournament assemble(TournamentInfoDTO dto, Object... params) {
        return tournamentRepository.findByRapidTournamentId(dto.getTournament().getRapidId())
                .map(
                tournament -> {
                    tournament.setCity(dto.getCity());
                    tournament.setGroundType(dto.getGroundType());
                    tournament.setMatchType(dto.getMatchType());

                    return tournament;
                }
        ).orElse(null);

    }
}
