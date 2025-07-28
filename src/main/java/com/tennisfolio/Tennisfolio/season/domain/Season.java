package com.tennisfolio.Tennisfolio.season.domain;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo.LeagueSeasonInfoDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons.LeagueSeasonsDTO;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Season {
    private Long seasonId;

    private Tournament tournament;

    private String seasonName;

    private String rapidSeasonId;

    private String year;

    private Long totalPrize;

    private String totalPrizeCurrency;

    private Long competitors;

    public Season(LeagueSeasonsDTO dto, Tournament tournament){
        this.tournament = tournament;
        this.seasonName = dto.getSeasonName();
        this.rapidSeasonId = dto.getSeasonRapidId();
        this.year = dto.getYear();
    }

    public void updateFromLeagueSeasonInfo(LeagueSeasonInfoDTO dto){
        if(totalPrize < 0L){
            throw new IllegalArgumentException("상금은 음수가 될 수 없습니다.");
        }
        this.totalPrize = dto.getTotalPrizeMoney();
        this.totalPrizeCurrency = dto.getCurrency();
        this.competitors = dto.getCompetitors();
    }
}
