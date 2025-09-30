package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Period;
import com.tennisfolio.Tennisfolio.match.domain.Score;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;

public class StatisticFixtures {

    public static Statistic allFirstServe(){
        return Statistic.builder()
                .statId(1L)
                .match(MatchFixtures.wimbledonMen2025FinalMatch())
                .groupName("First serve")
                .statDirection("positive")
                .metric("firstServeAccuracy")
                .period("ALL")
                .homeValue(40L)
                .awayValue(55L)
                .homeTotal(60L)
                .awayTotal(92L)
                .build();
    }

    public static Statistic allSecondServe(){
        return Statistic.builder()
                .statId(2L)
                .match(MatchFixtures.wimbledonMen2025FinalMatch())
                .groupName("Second serve")
                .statDirection("positive")
                .metric("secondServeAccuracy")
                .period("ALL")
                .homeValue(19L)
                .awayValue(31L)
                .homeTotal(20L)
                .awayTotal(37L)
                .build();
    }

    public static Statistic firstSetReturnPointsPoints(){
        return Statistic.builder()
                .statId(3L)
                .match(MatchFixtures.rolandGarrosMen2025FinalMatch())
                .groupName("First serve return points")
                .statDirection("positive")
                .metric("firstReturnPoints")
                .period("1ST")
                .homeValue(1L)
                .awayValue(8L)
                .homeTotal(17L)
                .awayTotal(27L)
                .build();
    }

    public static Statistic firstSetDoubleFaults(){
        return Statistic.builder()
                .statId(4L)
                .match(MatchFixtures.wimbledonMen2025FinalMatch())
                .groupName("Double faults")
                .statDirection("negative")
                .metric("doubleFaults")
                .period("1ST")
                .homeValue(0L)
                .awayValue(2L)
                .build();
    }
}
