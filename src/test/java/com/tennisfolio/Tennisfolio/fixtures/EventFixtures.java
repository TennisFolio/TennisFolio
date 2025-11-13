package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Period;
import com.tennisfolio.Tennisfolio.match.domain.Score;

public class EventFixtures {
    public static Match EndEvent(){
        return Match.builder()
                .matchId(2L)
                .rapidMatchId("2")
                .homeSeed("1")
                .awaySeed("2")
                .homeScore(2L)
                .awayScore(1L)
                .homeSet(homeScore())
                .awaySet(awayScore())
                .periodSet(periodSet())
                .startTimestamp("20251111043000")
                .winner("1")
                .status("Ended")
                .build();
    }

    public static Score homeScore(){
        return Score.builder()
                .set1(1L)
                .set2(6L)
                .set3(7L)
                .set3Tie(7L)
                .build();
    }

    public static Score awayScore(){
        return Score.builder()
                .set1(6L)
                .set2(3L)
                .set3(6L)
                .set3Tie(1L)
                .build();
    }

    public static Period periodSet(){
        return Period.builder()
                .set1("2000")
                .set2("3000")
                .set3("5000")
                .build();
    }
}
