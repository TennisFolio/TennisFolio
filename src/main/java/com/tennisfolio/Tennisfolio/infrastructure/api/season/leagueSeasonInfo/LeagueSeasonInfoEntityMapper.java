package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
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

        Season season = seasonRepository.findByRapidSeasonId(seasonRapidID)
                        .orElse(Season.builder().rapidSeasonId(seasonRapidID).build());

        season.updateFromLeagueSeasonInfo(dto.getTotalPrizeMoney(), dto.getCurrency(), dto.getCompetitors());

        return season;
    }
}
