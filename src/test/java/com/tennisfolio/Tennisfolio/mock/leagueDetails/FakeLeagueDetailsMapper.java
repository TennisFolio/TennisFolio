package com.tennisfolio.Tennisfolio.mock.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.fixtures.PlayerFixtures;
import com.tennisfolio.Tennisfolio.fixtures.TournamentFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails.LeagueDetailsDTO;
import com.tennisfolio.Tennisfolio.player.domain.Player;

public class FakeLeagueDetailsMapper implements EntityMapper<LeagueDetailsDTO, Tournament> {
    @Override
    public Tournament map(LeagueDetailsDTO dto, Object... params) {
        Player alcaraz = PlayerFixtures.alcaraz();
        Player nadal = PlayerFixtures.nadal();

        return Tournament
                .builder()
                .tournamentId(1L)
                .rapidTournamentId("2480")
                .mostTitlePlayer(nadal)
                .mostTitles("7")
                .titleHolder(alcaraz)
                .points(2000L)
                .startTimestamp("1750636800")
                .endTimestamp("1752364800")
                .build();
    }
}
