package com.tennisfolio.Tennisfolio.api.leagueSeasonInfo;

import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Component;

@Component
public class LeagueSeasonInfoMapper implements Mapper<LeagueSeasonInfoDTO, Season> {
    private final SeasonRepository seasonRepository;

    public LeagueSeasonInfoMapper(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Override
    public Season map(LeagueSeasonInfoDTO dto, Object... params) {
        String seasonRapidID = params[1].toString();

        Season findSeason = seasonRepository.findByRapidSeasonId(seasonRapidID).get();

        findSeason.setTotalPrize(dto.getTotalPrizeMoney());
        findSeason.setTotalPrizeCurrency(dto.getCurrency());
        findSeason.setCompetitors(dto.getCompetitors());

        return findSeason;
    }
}
