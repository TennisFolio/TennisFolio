package com.tennisfolio.Tennisfolio.Tournament.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.category.repository.CategoryEntity;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_tournament")
@Getter
@NoArgsConstructor
public class TournamentEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.TABLE, generator = "tournament_gen")
    @TableGenerator(
            name = "tournament_gen",
            table= "TB_SEQUENCES",
            pkColumnName= "TABLE_ID",
            valueColumnName= "NEXT_VAL",
            pkColumnValue = "TOURNAMENT_ID",
            allocationSize = 1000
    )
    private Long tournamentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private CategoryEntity categoryEntity;

    @Column(name = "RAPID_TOURNAMENT_ID")
    private String rapidTournamentId;

    @Column(name = "MATCH_TYPE")
    private String matchType;

    @Column(name = "TOURNAMENT_NAME")
    private String tournamentName;

    @Column(name = "CITY")
    private String city;

    @Column(name="GROUND_TYPE")
    private String groundType;

    @Column(name="LOGO")
    private String logo;

    @Column(name="MOST_TITLES")
    private String mostTitles;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="MOST_TITLE_PLAYER_ID", nullable = true)
    private PlayerEntity mostTitlePlayer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="TITLE_HOLDER_ID")
    private PlayerEntity titleHolder;

    @Column(name="POINTS")
    private Long points;

    public static TournamentEntity fromListModel(Tournament tournament) {
        TournamentEntity tournamentEntity = new TournamentEntity();
        tournamentEntity.tournamentId = tournament.getTournamentId();
        tournamentEntity.categoryEntity = tournament.getCategory() != null
                ? CategoryEntity.fromModel(tournament.getCategory())
                : new CategoryEntity();
        tournamentEntity.rapidTournamentId = tournament.getRapidTournamentId();
        tournamentEntity.tournamentName = tournament.getTournamentName();
        return tournamentEntity;
    }

    public static TournamentEntity fromModel(Tournament tournament) {
        TournamentEntity tournamentEntity = new TournamentEntity();
        tournamentEntity.tournamentId = tournament.getTournamentId();
        tournamentEntity.categoryEntity = tournament.getCategory() != null
                ? CategoryEntity.fromModel(tournament.getCategory())
                : new CategoryEntity();
        tournamentEntity.rapidTournamentId = tournament.getRapidTournamentId();
        tournamentEntity.matchType = tournament.getMatchType();
        tournamentEntity.tournamentName = tournament.getTournamentName();
        tournamentEntity.city = tournament.getCity();
        tournamentEntity.groundType = tournament.getGroundType();
        tournamentEntity.logo = tournament.getLogo();
        tournamentEntity.mostTitles = tournament.getMostTitles();
        tournamentEntity.mostTitlePlayer = tournament.getMostTitlePlayer() != null
                ? PlayerEntity.fromModel(tournament.getMostTitlePlayer())
                : null;
        tournamentEntity.titleHolder = tournament.getTitleHolder() != null
                ? PlayerEntity.fromModel(tournament.getTitleHolder())
                : null;
        tournamentEntity.points = tournament.getPoints();
        return tournamentEntity;
    }

    public Tournament toModel(){
        return Tournament.builder()
                .tournamentId(tournamentId)
                .category(categoryEntity.toModel())
                .rapidTournamentId(rapidTournamentId)
                .matchType(matchType)
                .tournamentName(tournamentName)
                .city(city)
                .groundType(groundType)
                .logo(logo)
                .mostTitles(mostTitles)
                .mostTitlePlayer(mostTitlePlayer != null
                        ? mostTitlePlayer.toModel()
                        : null)
                .titleHolder(titleHolder != null
                        ? titleHolder.toModel()
                        : null)
                .points(points)
                .build();
    }

    public Tournament toModelBaseOnly(){
        return Tournament.builder()
                .tournamentId(tournamentId)
                .category(categoryEntity != null ? categoryEntity.toModel() : null)
                .rapidTournamentId(rapidTournamentId)
                .matchType(matchType)
                .tournamentName(tournamentName)
                .city(city)
                .groundType(groundType)
                .logo(logo)
                .mostTitles(mostTitles)
                .points(points)
                .build();
    }
}
