package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Component;

@Component
public class LeagueSeasonInfoEntityMapper implements EntityMapper<LeagueSeasonInfoDTO, Season> {
    private final SeasonRepository seasonRepository;

    public LeagueSeasonInfoEntityMapper(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Override
    public Season map(LeagueSeasonInfoDTO dto, Object... params) {
        String seasonRapidID = params[1].toString();

        Season findSeason = seasonRepository.findByRapidSeasonId(seasonRapidID)
                .orElseThrow(() -> new IllegalArgumentException("조회되는 데이터가 없습니다."));

        findSeason.updateFromLeagueSeasonInfo(dto);

        return findSeason;
    }
}
