package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.season.domain.Season;

public class SeasonInfoFixtures {
    public static Season wimbledonMen2025(){
        return Season.builder()
                .seasonId(1L)
                .rapidSeasonId("63966")
                .seasonName("Wimbledon Men Singles 2025")
                .tournament(TournamentFixtures.wimbledonATP())
                .competitors(128L)
                .year("2025")
                .totalPrize(19414000L)
                .totalPrizeCurrency("EUR")
                .build();
    }

    public static Season wimbledonMen2024(){
        return Season.builder()
                .seasonId(2L)
                .rapidSeasonId("55487")
                .seasonName("Wimbledon Men Singles 2024")
                .tournament(TournamentFixtures.wimbledonATP())
                .competitors(128L)
                .year("2024")
                .totalPrize(17942000L)
                .totalPrizeCurrency("EUR")
                .build();
    }

    public static Season rolandGarrosMen2025(){
        return Season.builder()
                .seasonId(3L)
                .rapidSeasonId("61364")
                .seasonName("French Open Men Singles 2025")
                .tournament(TournamentFixtures.rolandGarrosATP())
                .competitors(128L)
                .year("2025")
                .totalPrize(20509000L)
                .totalPrizeCurrency("EUR")
                .build();
    }

    public static Season rolandGarrosMen2024(){
        return Season.builder()
                .seasonId(4L)
                .rapidSeasonId("52016")
                .seasonName("2024 French Open, Men Singles")
                .tournament(TournamentFixtures.wimbledonATP())
                .competitors(128L)
                .year("2024")
                .totalPrize(19280000L)
                .totalPrizeCurrency("EUR")
                .build();
    }

    public static Season unitedCup2025(){
        return Season.builder()
                .seasonId(15L)
                .rapidSeasonId("67129")
                .seasonName("United Cup 2025")
                .tournament(TournamentFixtures.unitedCupSingle())
                .competitors(18L)
                .year("2025")
                .totalPrize(10000000L)
                .totalPrizeCurrency("USD")
                .build();
    }

    public static Season laverCup2025(){
        return Season.builder()
                .seasonId(4L)
                .rapidSeasonId("67324")
                .seasonName("Exhibition Laver Cup Men Singles 2025")
                .tournament(TournamentFixtures.laverCup())
                .totalPrize(0L)
                .totalPrizeCurrency(null)
                .year("2025")
                .build();
    }
}
