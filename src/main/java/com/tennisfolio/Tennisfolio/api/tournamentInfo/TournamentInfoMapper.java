package com.tennisfolio.Tennisfolio.api.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoMapper implements Mapper<TournamentInfoDTO, Tournament> {
    private final EntityAssemble<TournamentInfoDTO, Tournament> tournamentInfoAssemble;

    public TournamentInfoMapper(EntityAssemble<TournamentInfoDTO, Tournament> tournamentInfoAssemble) {
        this.tournamentInfoAssemble = tournamentInfoAssemble;
    }

    @Override
    public Tournament map(TournamentInfoDTO dto) {
        return tournamentInfoAssemble.assemble(dto);
    }
}
