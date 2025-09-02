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

    public Tournament(Long tournamentId, Category category, String rapidTournamentId, String matchType, String tournamentName, String city
    , String groundType, String logo, String mostTitles, Player mostTitlePlayer, Player titleHolder, Long points){
        this.tournamentId = tournamentId;
        this.category = category;
        this.rapidTournamentId = rapidTournamentId;
        this.matchType = matchType;
        this.tournamentName = tournamentName;
        this.city = city;
        this.groundType = groundType;
        this.logo = logo;
        this.mostTitles = mostTitles;
        this.mostTitlePlayer = mostTitlePlayer;
        this.titleHolder = titleHolder;
        this.points = points;
    }

    public void updateFromLeagueDetails(Player mostTitlePlayer, Player titleHolder, LeagueDetailsDTO dto){
        this.mostTitlePlayer = mostTitlePlayer;
        this.titleHolder = titleHolder;
        this.mostTitles = dto.getMostTitles();
        this.points = dto.getPoints();
    }

    public void updateFromTournamentInfo(String city, String matchType, String groundType){
        this.city = city;
        this.matchType = matchType;
        this.groundType = groundType;
    }

    public void mergeTournament(Tournament tournament){
        this.mostTitlePlayer = tournament.getMostTitlePlayer();
        this.titleHolder = tournament.getTitleHolder();
        this.mostTitles = tournament.getMostTitles();
        this.points = tournament.getPoints();
    }

    public boolean needsTournamentInfo(){
        return this.city == null && this.matchType == null && this.groundType == null;
    }

    public boolean needsLeagueDetails(){
        return this.mostTitlePlayer == null && this.titleHolder == null && this.mostTitles == null && this.points == null;
    }

}
