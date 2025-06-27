package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Component;

@Component
public class LeagueSeasonInfoEntitySaver implements EntitySaver<Season> {
    private final SeasonRepository seasonRepository;

    public LeagueSeasonInfoEntitySaver(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Override
    public Season save(Season entity) {
        return seasonRepository.save(entity);
    }
}
