package com.tennisfolio.Tennisfolio.round.domain;

import com.tennisfolio.Tennisfolio.common.RoundType;
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

    public Round(Long round, String name, String slug, Season season){
        this.season = season;
        this.round = round == null? 0L : round;
        this.name = name;
        this.slug = slug;
    }

    public boolean isNew(){
        return roundId == null;
    }

    public void updateRoundInfo(RoundType roundType){
        this.name = roundType.getName();
        this.slug = roundType.getSlug();
    }

    public void updateSeason(Season season){
        this.season = season;
    }
}
