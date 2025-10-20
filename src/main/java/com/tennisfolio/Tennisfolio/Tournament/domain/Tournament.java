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

    private String startTimestamp;

    private String endTimestamp;

    public Tournament(){}

    public Tournament(Long tournamentId, Category category, String rapidTournamentId, String matchType, String tournamentName, String city
    , String groundType, String logo, String mostTitles, Player mostTitlePlayer, Player titleHolder, Long points, String startTimestamp, String endTimestamp){
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
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public void updateFromLeagueDetails(Player mostTitlePlayer, Player titleHolder, String mostTitles, Long points, String startTimestamp, String endTimestamp){
        this.mostTitlePlayer = mostTitlePlayer;
        this.titleHolder = titleHolder;
        this.mostTitles = mostTitles;
        this.points = points;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public void updateFromTournamentInfo(String city, String matchType, String groundType){
        this.city = city;
        this.matchType = matchType;
        this.groundType = groundType;
    }

    public void updateTimestamp(String startTimestamp, String endTimestamp){
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public void updateCategory(Category category){
        this.category = category;
    }

    public void updateTitles(Player mostTitlePlayer, Player titleHolder){
        this.mostTitlePlayer = mostTitlePlayer;
        this.titleHolder = titleHolder;
    }

    public boolean needsTournamentInfo(){
        return this.city == null && this.matchType == null && this.groundType == null;
    }

    public boolean needsLeagueDetails(){
        return this.mostTitlePlayer == null && this.titleHolder == null && this.mostTitles == null && this.points == null;
    }

    public boolean isMostTitlePlayerExists(){
        return this.getMostTitlePlayer() != null;
    }

    public boolean isTitleHolderExists(){
        return this.getTitleHolder() != null;
    }

}
