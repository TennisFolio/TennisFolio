package com.tennisfolio.Tennisfolio.mock.leagueRounds;

import com.tennisfolio.Tennisfolio.fixtures.RoundFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds.LeagueRoundsDTO;
import com.tennisfolio.Tennisfolio.round.domain.Round;

import java.util.List;

public class FakeLeagueRoundsMapper implements EntityMapper<List<LeagueRoundsDTO>, List<Round>> {
    @Override
    public List<Round> map(List<LeagueRoundsDTO> dto, Object... params) {
        return List.of(RoundFixtures.wimbledonMen2025SemiFinal(), RoundFixtures.wimbledonMen2025Final(),
                RoundFixtures.rolandGarrosMen2025SemiFinal(), RoundFixtures.rolandGarrosMen2025Final());
    }
}
