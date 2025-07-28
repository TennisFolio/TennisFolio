package com.tennisfolio.Tennisfolio.round.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds.LeagueRoundsDTO;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Round {

    private Long roundId;

    private Season season;

    private Long round;

    private String name;

    private String slug;

    public Round(LeagueRoundsDTO dto, Season season){
        this.season = season;
        this.round = dto.getRound();
        this.name = dto.getName();
        this.slug = dto.getSlug();
    }
}
