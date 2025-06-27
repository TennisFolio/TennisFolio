package com.tennisfolio.Tennisfolio.season.domain;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo.LeagueSeasonInfoDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons.LeagueSeasonsDTO;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tb_season")
@NoArgsConstructor
public class Season extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="SEASON_ID")
    private Long seasonId;
    @ManyToOne
    @JoinColumn(name = "TOURNAMENT_ID")
    private Tournament tournament;
    @Column(name = "SEASON_NAME")
    private String seasonName;
    @Column(name = "RAPID_SEASON_ID")
    private String rapidSeasonId;
    @Column(name="SEASON_YEAR")
    private String year;
    @Column(name="TOTAL_PRIZE")
    private Long totalPrize;
    @Column(name="TOTAL_PRIZE_CURRENCY")
    private String totalPrizeCurrency;
    @Column(name="COMPETITORS")
    private Long competitors;

    public Season(LeagueSeasonsDTO dto, Tournament tournament){
        this.tournament = tournament;
        this.seasonName = dto.getSeasonName();
        this.rapidSeasonId = dto.getSeasonRapidId();
        this.year = dto.getYear();
    }

    public void updateFromLeagueSeasonInfo(LeagueSeasonInfoDTO dto){
        this.totalPrize = dto.getTotalPrizeMoney();
        this.totalPrizeCurrency = dto.getCurrency();
        this.competitors = dto.getCompetitors();
    }
}
