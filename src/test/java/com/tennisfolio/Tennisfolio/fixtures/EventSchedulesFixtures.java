package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Period;
import com.tennisfolio.Tennisfolio.match.domain.Score;

public class EventSchedulesFixtures {

    public static Match wimbledonMen2025QuarterFinalsMatch1(){
        return Match.builder()
                .matchId(1L)
                .rapidMatchId("1")
                .round(RoundFixtures.wimbledonMen2025QuarterFinal())
                .homePlayer(PlayerFixtures.fritz())
                .awayPlayer(PlayerFixtures.khachanov())
                .startTimeStamp("20251011080000")
                .build();
    }

    public static Match wimbledonMen2025QuarterFinalsMatch2(){
        return Match.builder()
                .matchId(2L)
                .rapidMatchId("2")
                .round(RoundFixtures.wimbledonMen2025QuarterFinal())
                .homePlayer(PlayerFixtures.norrie())
                .awayPlayer(PlayerFixtures.alcaraz())
                .startTimeStamp("20251011100000")
                .build();
    }

    public static Match wimbledonMen2025QuarterFinalsMatch3(){
        return Match.builder()
                .matchId(3L)
                .rapidMatchId("3")
                .round(RoundFixtures.wimbledonMen2025QuarterFinal())
                .homePlayer(PlayerFixtures.sinner())
                .awayPlayer(PlayerFixtures.shelton())
                .startTimeStamp("20251011120000")
                .build();
    }

    public static Match wimbledonMen2025QuarterFinalsMatch4(){
        return Match.builder()
                .matchId(4L)
                .rapidMatchId("4")
                .round(RoundFixtures.wimbledonMen2025QuarterFinal())
                .homePlayer(PlayerFixtures.cobolli())
                .awayPlayer(PlayerFixtures.djokovic())
                .startTimeStamp("20251011140000")
                .build();
    }


}
