package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Period;
import com.tennisfolio.Tennisfolio.match.domain.Score;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchPlayerResponse;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchScoreResponse;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchTimeResponse;

import java.util.List;

public class LiveEventsFixtures {

    public static LiveMatchPlayerResponse homePlayer(){
        return  LiveMatchPlayerResponse.builder()
                .playerRapidId(PlayerFixtures.alcaraz().getRapidPlayerId())
                .playerName(PlayerFixtures.alcaraz().getPlayerName())
                .playerRanking("1")
                .build();
    }

    public static LiveMatchPlayerResponse awayPlayer(){
        return LiveMatchPlayerResponse.builder()
                .playerRapidId(PlayerFixtures.sinner().getRapidPlayerId())
                .playerName(PlayerFixtures.sinner().getPlayerName())
                .playerRanking("2")
                .build();
    }

    public static LiveMatchScoreResponse homeScoreInProgress1(){
        return LiveMatchScoreResponse.builder()
                .current(1L)
                .display(1L)
                .point("15")
                .periodScore(List.of(6L,1L,2L))
                .build();
    }

    public static LiveMatchScoreResponse awayScoreInProgress1(){
        return LiveMatchScoreResponse.builder()
                .current(1L)
                .display(1L)
                .point("30")
                .periodScore(List.of(2L,6L,3L))
                .build();
    }

    public static LiveMatchScoreResponse homeScoreInProgress2(){
        return LiveMatchScoreResponse.builder()
                .current(1L)
                .display(1L)
                .point("40")
                .periodScore(List.of(6L,1L,2L))
                .build();
    }

    public static LiveMatchScoreResponse awayScoreInProgress2(){
        return LiveMatchScoreResponse.builder()
                .current(1L)
                .display(1L)
                .point("40")
                .periodScore(List.of(2L,6L,3L))
                .build();
    }

    public static LiveMatchScoreResponse homeScoreInProgress3(){
        return LiveMatchScoreResponse.builder()
                .current(1L)
                .display(1L)
                .point("30")
                .periodScore(List.of(6L,1L,2L))
                .build();
    }

    public static LiveMatchScoreResponse awayScoreInProgress3(){
        return LiveMatchScoreResponse.builder()
                .current(1L)
                .display(1L)
                .point("40")
                .periodScore(List.of(2L,6L,5L))
                .build();
    }

    public static LiveMatchTimeResponse timeInProgress1(){
        return LiveMatchTimeResponse.builder()
                .startTime("1762128000")
                .currentPeriodStartTimestamp("1762583573")
                .period(List.of("1829", "2953", "2228"))
                .build();
    }

    public static LiveMatchTimeResponse timeInProgress2(){
        return LiveMatchTimeResponse.builder()
                .startTime("1762128000")
                .currentPeriodStartTimestamp("1762583573")
                .period(List.of("1829", "2953", "2500"))
                .build();
    }

    public static LiveMatchTimeResponse timeInProgress3(){
        return LiveMatchTimeResponse.builder()
                .startTime("1762128000")
                .currentPeriodStartTimestamp("1762583573")
                .period(List.of("1900", "3000", "3500"))
                .build();
    }

    public static LiveMatchResponse liveMatchInProgress1(){

        return LiveMatchResponse.builder()
                .rapidId("1")
                .category("atp")
                .tournamentName(TournamentFixtures.wimbledonATP().getTournamentName())
                .homePlayer(homePlayer())
                .awayPlayer(awayPlayer())
                .seasonName(SeasonFixtures.wimbledonMen2025().getSeasonName())
                .roundName(RoundFixtures.wimbledonMen2025Final().getName())
                .homeScore(homeScoreInProgress1())
                .awayScore(awayScoreInProgress1())
                .time(timeInProgress1())
                .status("3rd Set")
                .build();
    }

    public static LiveMatchResponse liveMatchInProgress2(){

        return LiveMatchResponse.builder()
                .rapidId("1")
                .category("atp")
                .tournamentName(TournamentFixtures.wimbledonATP().getTournamentName())
                .homePlayer(homePlayer())
                .awayPlayer(awayPlayer())
                .seasonName(SeasonFixtures.wimbledonMen2025().getSeasonName())
                .roundName(RoundFixtures.wimbledonMen2025Final().getName())
                .homeScore(homeScoreInProgress2())
                .awayScore(awayScoreInProgress2())
                .time(timeInProgress2())
                .status("3rd Set")
                .build();
    }

    public static LiveMatchResponse liveMatchInProgress3(){

        return LiveMatchResponse.builder()
                .rapidId("2")
                .category("atp")
                .tournamentName(TournamentFixtures.wimbledonATP().getTournamentName())
                .homePlayer(homePlayer())
                .awayPlayer(awayPlayer())
                .seasonName(SeasonFixtures.wimbledonMen2025().getSeasonName())
                .roundName(RoundFixtures.wimbledonMen2025Final().getName())
                .homeScore(homeScoreInProgress3())
                .awayScore(awayScoreInProgress3())
                .time(timeInProgress3())
                .status("3rd set")
                .build();
    }

}
