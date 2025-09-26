package com.tennisfolio.Tennisfolio.mock.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.fixtures.MatchFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;

import java.util.List;

public class FakeLeagueEventsByRoundMapper implements EntityMapper<List<LeagueEventsByRoundDTO>, List<Match>> {

    @Override
    public List<Match> map(List<LeagueEventsByRoundDTO> dto, Object... params) {
        String rapidSeasonId = params[1].toString();
        String slug = params[3].toString();
        List<Match> matches = List.of(MatchFixtures.rolandGarrosMen2025SemiFinalMatch(), MatchFixtures.rolandGarrosMen2025FinalMatch(),  MatchFixtures.wimbledonMen2025SemiFinalMatch(), MatchFixtures.wimbledonMen2025FinalMatch());

        return matches.stream().filter(p -> p.getRound().getSeason().isSameSeason(rapidSeasonId) &&
                slug.equals(p.getRound().getSlug())).toList();
    }
}
