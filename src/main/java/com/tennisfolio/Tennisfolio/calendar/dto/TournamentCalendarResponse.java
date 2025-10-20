package com.tennisfolio.Tennisfolio.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TournamentCalendarResponse {

    private Long categoryId;

    private String categoryName;

    private Long tournamentId;

    private String tournamentName;

    private Long seasonId;

    private String seasonName;

    private String year;

    private String startTimestamp;

    private String endTimestamp;

    @Builder
    public TournamentCalendarResponse(Long categoryId, String categoryName,Long tournamentId, String tournamentName, Long seasonId, String seasonName, String year,
                                      String startTimestamp, String endTimestamp){
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;
        this.seasonId =seasonId;
        this.seasonName = seasonName;
        this.year = year;
        this.startTimestamp = startTimestamp.substring(0, 8);
        this.endTimestamp = endTimestamp.substring(0, 8);
    }


}
