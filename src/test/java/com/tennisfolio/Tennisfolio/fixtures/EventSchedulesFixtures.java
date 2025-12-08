package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Period;
import com.tennisfolio.Tennisfolio.match.domain.Score;

public class EventSchedulesFixtures {

    public static Match wimbledonMen2025QuarterFinalsMatch1(){
        return Match.builder()
                .matchId(1L)
                .rapidMatchId("1")
                .homeScore(0L)
                .awayScore(0L)
                .round(RoundFixtures.wimbledonMen2025QuarterFinal())
                .homePlayer(PlayerFixtures.fritz())
                .awayPlayer(PlayerFixtures.khachanov())
                .startTimestamp("20251011080000")
                .status("Ended")
                .build();
    }

    public static Match wimbledonMen2025QuarterFinalsMatch2(){
        return Match.builder()
                .matchId(2L)
                .rapidMatchId("2")
                .homeScore(0L)
                .awayScore(0L)
                .round(RoundFixtures.wimbledonMen2025QuarterFinal())
                .homePlayer(PlayerFixtures.norrie())
                .awayPlayer(PlayerFixtures.alcaraz())
                .startTimestamp("20251011100000")
                .status("Ended")
                .build();
    }

    public static Match wimbledonMen2025QuarterFinalsMatch3(){
        return Match.builder()
                .matchId(3L)
                .rapidMatchId("3")
                .homeScore(0L)
                .awayScore(0L)
                .round(RoundFixtures.wimbledonMen2025QuarterFinal())
                .homePlayer(PlayerFixtures.sinner())
                .awayPlayer(PlayerFixtures.shelton())
                .startTimestamp("20251011120000")
                .status("Ended")
                .build();
    }

    public static Match wimbledonMen2025QuarterFinalsMatch4(){
        return Match.builder()
                .matchId(4L)
                .rapidMatchId("4")
                .homeScore(1L)
                .awayScore(3L)
                .winner("2")
                .round(RoundFixtures.wimbledonMen2025QuarterFinal())
                .homePlayer(PlayerFixtures.cobolli())
                .awayPlayer(PlayerFixtures.djokovic())
                .startTimestamp("20251011140000")
                .status("Ended")
                .build();
    }

    public static Match wimbledonMen2025Duplicated(){
        return Match.builder()
                .matchId(5L)
                .rapidMatchId("4")
                .round(RoundFixtures.wimbledonMen2025QuarterFinal())
                .homePlayer(PlayerFixtures.cobolli())
                .awayPlayer(PlayerFixtures.djokovic())
                .startTimestamp("20251011140000")
                .homeScore(3L)
                .awayScore(2L)
                .winner("1")
                .status("Ended")
                .build();
    }


}
