package com.tennisfolio.Tennisfolio.season.domain;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo.LeagueSeasonInfoDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons.LeagueSeasonsDTO;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
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

    private String startTimestamp;

    private String endTimestamp;

    private static final int FIRST_VALID_YEAR = 2019;

    public Season(LeagueSeasonsDTO dto, Tournament tournament){
        this.tournament = tournament;
        this.seasonName = dto.getSeasonName();
        this.rapidSeasonId = dto.getSeasonRapidId();
        this.year = dto.getYear();
    }

    public void updateFromLeagueSeasonInfo(Long totalPrize, String totalPrizeCurrency, Long competitors){
        this.totalPrize =totalPrize == null ? 0L : totalPrize;
        this.totalPrizeCurrency = totalPrizeCurrency;
        this.competitors = competitors;
    }

    public void updateTimestamp(){

        this.startTimestamp = tournament.getStartTimestamp() != null?
                ConversionUtil.timestampToYyyyMMddHHMMSS(tournament.getStartTimestamp()) : "";

        this.endTimestamp = tournament.getEndTimestamp() != null?
                ConversionUtil.timestampToYyyyMMddHHMMSS(tournament.getEndTimestamp()) : "";
    }

    public void updateTournament(Tournament tournament){
        this.tournament = tournament;
    }

    public boolean isSince2019(){
        try{
            return Integer.parseInt(this.year) >= FIRST_VALID_YEAR;
        }catch(NumberFormatException e){
            return false;
        }

    }

    public boolean needsLeagueSeasonInfo(){
        return totalPrize == null && totalPrizeCurrency == null && competitors == null && startTimestamp == null && endTimestamp == null;
    }
    public boolean isNew(){
        return seasonId == null;
    }


    public boolean isSameSeason(String rapidSeasonId){
        return this.rapidSeasonId.equals(rapidSeasonId);
    }
}
