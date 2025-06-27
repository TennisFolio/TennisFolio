package com.tennisfolio.Tennisfolio.Tournament.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments.CategoryTournamentsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;
import com.tennisfolio.Tennisfolio.category.domain.model.Category;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_tournament")
@Getter
@NoArgsConstructor
public class Tournament extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="TOURNAMENT_ID")
    private Long tournamentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

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
    private Player mostTitlePlayer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="TITLE_HOLDER_ID")
    private Player titleHolder;

    @Column(name="POINTS")
    private Long points;

    public Tournament(CategoryTournamentsDTO dto, Category category){
        this.rapidTournamentId = dto.getTournamentRapidId();
        this.tournamentName = dto.getTournamentName();
        this.category = category;
    }

//    public Tournament(TournamentInfoDTO dto){
//        this.city = dto.getCity();
//        this.matchType = dto.getMatchType();
//        this.groundType = dto.getGroundType();
//    }


    public void updatePlayers(Player mostTitlePlayer, Player titleHolder){
        this.mostTitlePlayer = mostTitlePlayer;
        this.titleHolder = titleHolder;
    }

    public void updateFromTournamentInfo(TournamentInfoDTO incoming){
        this.city = incoming.getCity();
        this.matchType = incoming.getMatchType();
        this.groundType = incoming.getGroundType();
    }
}
