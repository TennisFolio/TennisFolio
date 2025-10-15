package com.tennisfolio.Tennisfolio.season.repository;

import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo.LeagueSeasonInfoDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons.LeagueSeasonsDTO;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundEntity;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tb_season")
@NoArgsConstructor
public class SeasonEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.TABLE, generator = "season_gen")
    @TableGenerator(
            name="season_gen",
            table="TB_SEQUENCES",
            pkColumnName = "TABLE_ID",
            valueColumnName = "NEXT_VAL",
            pkColumnValue = "SEASON_ID",
            allocationSize = 1000
    )
    private Long seasonId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TOURNAMENT_ID")
    private TournamentEntity tournamentEntity;
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
    @Column(name="START_TIMESTAMP")
    private String startTimestamp;
    @Column(name="END_TIMESTAMP")
    private String endTimestamp;


    public static SeasonEntity fromModel(Season season) {
        SeasonEntity seasonEntity = new SeasonEntity();
        seasonEntity.seasonId = season.getSeasonId();
        seasonEntity.tournamentEntity = TournamentEntity.fromModel(season.getTournament());
        seasonEntity.seasonName = season.getSeasonName();
        seasonEntity.rapidSeasonId = season.getRapidSeasonId();
        seasonEntity.year = season.getYear();
        seasonEntity.totalPrize = season.getTotalPrize();
        seasonEntity.totalPrizeCurrency = season.getTotalPrizeCurrency();
        seasonEntity.competitors = season.getCompetitors();
        seasonEntity.startTimestamp = season.getStartTimestamp();
        seasonEntity.endTimestamp = season.getEndTimestamp();

        return seasonEntity;
    }

    public Season toModel(){
        return Season.builder()
                .seasonId(seasonId)
                .tournament(tournamentEntity.toModelBaseOnly())
                .seasonName(seasonName)
                .rapidSeasonId(rapidSeasonId)
                .year(year)
                .totalPrize(totalPrize)
                .totalPrizeCurrency(totalPrizeCurrency)
                .competitors(competitors)
                .startTimestamp(startTimestamp)
                .endTimestamp(endTimestamp)
                .build();
    }

    public Season toModelBaseOnly(){
        return Season.builder()
                .seasonId(seasonId)
                .seasonName(seasonName)
                .rapidSeasonId(rapidSeasonId)
                .year(year)
                .totalPrize(totalPrize)
                .totalPrizeCurrency(totalPrizeCurrency)
                .competitors(competitors)
                .startTimestamp(startTimestamp)
                .endTimestamp(endTimestamp)
                .build();
    }
}
