package com.tennisfolio.Tennisfolio.mock.leagueSeasonInfo;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.fixtures.SeasonInfoFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo.LeagueSeasonInfoDTO;
import com.tennisfolio.Tennisfolio.season.domain.Season;

import java.util.List;

public class FakeLeagueSeasonInfoMapper implements EntityMapper<LeagueSeasonInfoDTO, Season> {
    @Override
    public Season map(LeagueSeasonInfoDTO dto, Object... params) {
        String rapidSeasonId = params[1].toString();

        List<Season> seasonList = List.of(SeasonInfoFixtures.wimbledonMen2024()
                , SeasonInfoFixtures.wimbledonMen2025()
                , SeasonInfoFixtures.rolandGarrosMen2024(),
                SeasonInfoFixtures.rolandGarrosMen2025());

        return seasonList
                .stream()
                .filter(season -> rapidSeasonId.equals(season.getRapidSeasonId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }
}
