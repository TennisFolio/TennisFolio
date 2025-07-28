package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.repository.TournamentJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoAssemble implements EntityAssemble<TournamentInfoDTO, Tournament> {
    private final TournamentJpaRepository tournamentJpaRepository;

    public TournamentInfoAssemble(TournamentJpaRepository tournamentJpaRepository) {
        this.tournamentJpaRepository = tournamentJpaRepository;
    }

    @Override
    public Tournament assemble(TournamentInfoDTO dto, Object... params) {
        return tournamentJpaRepository.findByRapidTournamentId(dto.getTournament().getRapidId())
                .map(entity -> entity.toModel())
                .map(
                tournament -> {
                    tournament.updateFromTournamentInfo(dto);

                    return tournament;
                }
        ).orElseThrow(() -> new IllegalArgumentException("조회되는 데이터가 없습니다."));

    }
}
