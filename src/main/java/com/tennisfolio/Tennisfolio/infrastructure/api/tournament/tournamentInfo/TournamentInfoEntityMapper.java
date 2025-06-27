package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoEntityMapper implements EntityMapper<TournamentInfoDTO, Tournament> {
    private final EntityAssemble<TournamentInfoDTO, Tournament> tournamentInfoAssemble;

    public TournamentInfoEntityMapper(EntityAssemble<TournamentInfoDTO, Tournament> tournamentInfoAssemble) {
        this.tournamentInfoAssemble = tournamentInfoAssemble;
    }

    @Override
    public Tournament map(TournamentInfoDTO dto, Object... params) {
        return tournamentInfoAssemble.assemble(dto);
    }
}
