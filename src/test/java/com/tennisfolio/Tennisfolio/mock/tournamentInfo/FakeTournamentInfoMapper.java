package com.tennisfolio.Tennisfolio.mock.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.fixtures.TournamentFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;

public class FakeTournamentInfoMapper implements EntityMapper<TournamentInfoDTO, Tournament> {
    @Override
    public Tournament map(TournamentInfoDTO dto, Object... params) {
        return Tournament
                .builder()
                .rapidTournamentId("2480")
                .city("Paris")
                .matchType("singles")
                .groundType("clay")
                .build();
    }
}
