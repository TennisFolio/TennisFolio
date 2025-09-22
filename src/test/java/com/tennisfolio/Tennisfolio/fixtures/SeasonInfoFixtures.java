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
}
