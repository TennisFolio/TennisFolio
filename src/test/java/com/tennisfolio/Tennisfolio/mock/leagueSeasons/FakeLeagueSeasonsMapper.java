package com.tennisfolio.Tennisfolio.mock.leagueSeasons;

import com.tennisfolio.Tennisfolio.fixtures.SeasonFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons.LeagueSeasonsDTO;
import com.tennisfolio.Tennisfolio.season.domain.Season;

import java.util.List;
import java.util.stream.Collectors;

public class FakeLeagueSeasonsMapper implements EntityMapper<List<LeagueSeasonsDTO>, List<Season>> {
    @Override
    public List<Season> map(List<LeagueSeasonsDTO> dto, Object... params) {

        String tournamentRapidId = params[0].toString();
        return List.of(
                SeasonFixtures.wimbledonMen2025(),
                SeasonFixtures.wimbledonMen2024(),
                SeasonFixtures.rolandGarrosMen2025(),
                SeasonFixtures.rolandGarrosMen2024()
        )
                .stream()
                .filter(season -> tournamentRapidId.equals(season.getTournament().getRapidTournamentId()))
                .collect(Collectors.toList());
    }
}
