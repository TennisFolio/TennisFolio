package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.season.domain.Season;

public class RoundFixtures {
    public static Round wimbledonMen2025SemiFinal(){

        return Round.builder()
                .roundId(1L)
                .season(SeasonInfoFixtures.wimbledonMen2025())
                .round(28L)
                .name("Semifinals")
                .slug("semifinals")
                .build();
    }

    public static Round wimbledonMen2025Final(){
        return Round.builder()
                .roundId(2L)
                .season(SeasonInfoFixtures.wimbledonMen2025())
                .round(29L)
                .name("Final")
                .slug("final")
                .build();
    }

    public static Round rolandGarrosMen2025SemiFinal(){
        return Round.builder()
                .roundId(3L)
                .season(SeasonInfoFixtures.rolandGarrosMen2025())
                .round(28L)
                .name("Semifinals")
                .slug("semifinals")
                .build();
    }

    public static Round rolandGarrosMen2025Final(){
        return Round.builder()
                .roundId(4L)
                .season(SeasonInfoFixtures.rolandGarrosMen2025())
                .round(29L)
                .name("Final")
                .slug("finals")
                .build();
    }

    public static Round wimbledonMen2025QuarterFinal(){
        return Round.builder()
                .roundId(5L)
                .season(SeasonInfoFixtures.wimbledonMen2025())
                .round(27L)
                .name("Quarterfinals")
                .slug("quarterfinals")
                .build();
    }

    public static Round laverCup2025SemiFinal(){
        return Round.builder()
                .roundId(10L)
                .season(SeasonInfoFixtures.laverCup2025())
                .round(28L)
                .name("Semifinals")
                .slug("semifinals")
                .build();
    }

    public static Round unitedCup2025UNKNOWN(){
        return Round.builder()
                .roundId(11803L)
                .season(SeasonInfoFixtures.unitedCup2025())
                .round(0L)
                .name("UNKNOWN")
                .slug("UNKNOWN")
                .build();
    }
}
