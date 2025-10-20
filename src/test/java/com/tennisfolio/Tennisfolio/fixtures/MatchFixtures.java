package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Period;
import com.tennisfolio.Tennisfolio.match.domain.Score;
import com.tennisfolio.Tennisfolio.round.domain.Round;

public class MatchFixtures {


    public static Match wimbledonMen2025FinalMatch(){
        return Match.builder()
                .matchId(1L)
                .rapidMatchId("1")
                .round(RoundFixtures.wimbledonMen2025Final())
                .homePlayer(PlayerFixtures.alcaraz())
                .awayPlayer(PlayerFixtures.nadal())
                .homeSet(new Score(5L,7L,2L,0L,0L,0L,7L,0L,0L,0L))
                .awaySet(new Score(7L,6L,6L,0L,0L,0L,2L,0L,0L,0L))
                .homeSeed("1")
                .awaySeed("2")
                .startTimestamp("20250105")
                .winner("2")
                .periodSet(new Period("38:33", "50:22", "40:32", "0", "0"))
                .homeScore(2L)
                .awayScore(1L)
                .build();
    }

    public static Match rolandGarrosMen2025FinalMatch(){
        return Match.builder()
                .matchId(2L)
                .rapidMatchId("2")
                .round(RoundFixtures.rolandGarrosMen2025Final())
                .homePlayer(PlayerFixtures.nadal())
                .awayPlayer(PlayerFixtures.federer())
                .homeSet(new Score(7L,7L,0L,0L,0L,7L,7L,0L,0L,0L))
                .awaySet(new Score(6L,6L,0L,0L,0L,5L,2L,0L,0L,0L))
                .homeSeed("1")
                .awaySeed("2")
                .startTimestamp("20250103")
                .winner("1")
                .periodSet(new Period("55:33", "50:22", "0", "0", "0"))
                .homeScore(2L)
                .awayScore(0L)
                .build();
    }

    public static Match wimbledonMen2025SemiFinalMatch(){

        return Match.builder()
                .matchId(3L)
                .rapidMatchId("3")
                .round(RoundFixtures.wimbledonMen2025SemiFinal())
                .homePlayer(PlayerFixtures.alcaraz())
                .awayPlayer(PlayerFixtures.sinner())
                .homeSet(new Score(7L,4L,6L,4L,7L,7L,0L,0L,0L,7L))
                .awaySet(new Score(6L,6L,1L,6L,6L,4L,0L,0L,0L,3L))
                .homeSeed("1")
                .awaySeed("2")
                .startTimestamp("20250102")
                .winner("1")
                .periodSet(new Period("62:31", "50:22", "36:32", "50:35", "63;21"))
                .homeScore(3L)
                .awayScore(2L)
                .build();
    }

    public static Match rolandGarrosMen2025SemiFinalMatch(){
        return Match.builder()
                .matchId(4L)
                .rapidMatchId("4")
                .round(RoundFixtures.rolandGarrosMen2025SemiFinal())
                .homePlayer(PlayerFixtures.federer())
                .awayPlayer(PlayerFixtures.sinner())
                .homeSet(new Score(7L,7L,0L,0L,0L,7L,7L,0L,0L,0L))
                .awaySet(new Score(6L,6L,0L,0L,0L,5L,2L,0L,0L,0L))
                .homeSeed("1")
                .awaySeed("2")
                .startTimestamp("20250103")
                .winner("1")
                .periodSet(new Period("55:33", "50:22", "0", "0", "0"))
                .homeScore(2L)
                .awayScore(0L)
                .build();
    }


}
