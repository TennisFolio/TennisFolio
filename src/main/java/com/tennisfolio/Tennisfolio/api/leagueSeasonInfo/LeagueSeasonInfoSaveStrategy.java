package com.tennisfolio.Tennisfolio.api.leagueSeasonInfo;

import com.tennisfolio.Tennisfolio.api.base.SaveStrategy;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Component;

@Component
public class LeagueSeasonInfoSaveStrategy implements SaveStrategy<Season> {
    private final SeasonRepository seasonRepository;

    public LeagueSeasonInfoSaveStrategy(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Override
    public Season save(Season entity) {
        return seasonRepository.save(entity);
    }
}
