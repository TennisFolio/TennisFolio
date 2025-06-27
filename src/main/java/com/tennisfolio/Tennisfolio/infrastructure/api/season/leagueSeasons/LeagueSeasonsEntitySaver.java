package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeagueSeasonsEntitySaver implements EntitySaver<List<Season>> {
    private final SeasonRepository seasonRepository;

    public LeagueSeasonsEntitySaver(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Override
    public List<Season> save(List<Season> entity) {
        List<Season> toSave = entity.stream()
                .filter(season -> seasonRepository.findByRapidSeasonId(season.getRapidSeasonId()).isEmpty())
                .collect(Collectors.toList());

        return seasonRepository.saveAll(toSave);
    }
}
