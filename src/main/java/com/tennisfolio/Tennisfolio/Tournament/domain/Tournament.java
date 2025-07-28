package com.tennisfolio.Tennisfolio.Tournament.domain;

import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments.CategoryTournamentsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails.LeagueDetailsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor
public class Tournament{

    private Long tournamentId;

    private Category category;

    private String rapidTournamentId;

    private String matchType;

    private String tournamentName;

    private String city;

    private String groundType;

    private String logo;

    private String mostTitles;

    private Player mostTitlePlayer;

    private Player titleHolder;

    private Long points;

    public Tournament(){}

    public Tournament(CategoryTournamentsDTO dto, Category category){
        this.rapidTournamentId = dto.getTournamentRapidId();
        this.tournamentName = dto.getTournamentName();
        this.category = category;
    }

    public void updateFromLeagueDetails(Player mostTitlePlayer, Player titleHolder, LeagueDetailsDTO dto){
        this.mostTitlePlayer = mostTitlePlayer;
        this.titleHolder = titleHolder;
        this.mostTitles = dto.getMostTitles();
        this.points = dto.getPoints();
    }

    public void updateFromTournamentInfo(TournamentInfoDTO incoming){
        this.city = incoming.getCity();
        this.matchType = incoming.getMatchType();
        this.groundType = incoming.getGroundType();
    }

    public void mergeTournament(Tournament tournament){
        this.mostTitlePlayer = tournament.getMostTitlePlayer();
        this.titleHolder = tournament.getTitleHolder();
        this.mostTitles = tournament.getMostTitles();
        this.points = tournament.getPoints();
    }

}
