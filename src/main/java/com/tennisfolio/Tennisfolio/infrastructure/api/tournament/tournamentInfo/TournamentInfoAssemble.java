package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
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
                    tournament.updateFromTournamentInfo(dto);

                    return tournament;
                }
        ).orElseThrow(() -> new IllegalArgumentException("조회되는 데이터가 없습니다."));

    }
}
