package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.match.dto.LiveMatchPlayerResponse;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchScoreResponse;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchTimeResponse;

import java.util.List;

public class EtcLiveEventsFixtures {

    public static LiveMatchPlayerResponse homePlayer(){
        return  LiveMatchPlayerResponse.builder()
                .playerRapidId(PlayerFixtures.alcaraz().getRapidPlayerId())
                .playerName(PlayerFixtures.alcaraz().getPlayerName())
                .build();
    }

    public static LiveMatchPlayerResponse awayPlayer(){
        return LiveMatchPlayerResponse.builder()
                .playerRapidId(PlayerFixtures.sinner().getRapidPlayerId())
                .playerName(PlayerFixtures.sinner().getPlayerName())
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
                .startTime("1767182400")
                .currentPeriodStartTimestamp("1767182400")
                .period(List.of("1829", "2953", "2228"))
                .build();
    }

    public static LiveMatchTimeResponse timeInProgress2(){
        return LiveMatchTimeResponse.builder()
                .startTime("1767182400")
                .currentPeriodStartTimestamp("1767184200")
                .period(List.of("1829", "2953", "2500"))
                .build();
    }

    public static LiveMatchTimeResponse timeInProgress3(){
        return LiveMatchTimeResponse.builder()
                .startTime("1767182400")
                .currentPeriodStartTimestamp("1767186000")
                .period(List.of("1900", "3000", "3500"))
                .build();
    }

    public static LiveMatchResponse etcLiveMatchInProgress1(){

        return LiveMatchResponse.builder()
                .rapidId("5")
                .category(CategoryFixtures.exhibition().getCategorySlug())
                .tournamentName(TournamentFixtures.laverCup().getTournamentName())
                .homePlayer(homePlayer())
                .awayPlayer(awayPlayer())
                .seasonName(SeasonFixtures.laverCup2025().getSeasonName())
                .roundName(RoundFixtures.laverCup2025SemiFinal().getName())
                .homeScore(homeScoreInProgress1())
                .awayScore(awayScoreInProgress1())
                .time(timeInProgress1())
                .status("3rd Set")
                .build();
    }

    public static LiveMatchResponse etcLiveMatchInProgress2(){

        return LiveMatchResponse.builder()
                .rapidId("6")
                .category(CategoryFixtures.unitedCup().getCategorySlug())
                .tournamentName(TournamentFixtures.unitedCupSingle().getTournamentName())
                .homePlayer(homePlayer())
                .awayPlayer(awayPlayer())
                .seasonName(SeasonFixtures.unitedCup2025().getSeasonName())
                .roundName(RoundFixtures.unitedCup2025UNKNOWN().getName())
                .homeScore(homeScoreInProgress2())
                .awayScore(awayScoreInProgress2())
                .time(timeInProgress2())
                .status("3rd Set")
                .build();
    }

}
