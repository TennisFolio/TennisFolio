package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;

public class PlayerPrizeFixtures {

    public static PlayerPrize nadal() {
        return PlayerPrize.builder()
                .prizeId(1L)
                .player(PlayerFixtures.nadal())
                .prizeCurrentAmount(10L)
                .prizeCurrentCurrency("EUR")
                .prizeTotalAmount(200L)
                .prizeTotalCurrency("EUR")
                .build();
    }

    public static PlayerPrize alcaraz() {
        return PlayerPrize.builder()
                .prizeId(2L)
                .player(PlayerFixtures.alcaraz())
                .prizeCurrentAmount(50L)
                .prizeCurrentCurrency("EUR")
                .prizeTotalAmount(100L)
                .prizeTotalCurrency("EUR")
                .build();
    }

    public static PlayerPrize sinner() {
        return PlayerPrize.builder()
                .prizeId(3L)
                .player(PlayerFixtures.sinner())
                .prizeCurrentAmount(40L)
                .prizeCurrentCurrency("EUR")
                .prizeTotalAmount(120L)
                .prizeTotalCurrency("EUR")
                .build();
    }

    public static PlayerPrize nadalUpdate() {
        return PlayerPrize.builder()
                .prizeId(1L)
                .player(PlayerFixtures.nadal())
                .prizeCurrentAmount(20L)
                .prizeCurrentCurrency("EUR")
                .prizeTotalAmount(400L)
                .prizeTotalCurrency("EUR")
                .build();
    }

    public static PlayerPrize alcarazUpdate() {
        return PlayerPrize.builder()
                .prizeId(2L)
                .player(PlayerFixtures.alcaraz())
                .prizeCurrentAmount(100L)
                .prizeCurrentCurrency("EUR")
                .prizeTotalAmount(200L)
                .prizeTotalCurrency("EUR")
                .build();
    }

    public static PlayerPrize sinnerUpdate() {
        return PlayerPrize.builder()
                .prizeId(3L)
                .player(PlayerFixtures.sinner())
                .prizeCurrentAmount(80L)
                .prizeCurrentCurrency("EUR")
                .prizeTotalAmount(240L)
                .prizeTotalCurrency("EUR")
                .build();
    }

    public static PlayerPrize federer() {
        return PlayerPrize.builder()
                .prizeId(4L)
                .player(PlayerFixtures.federer())
                .prizeCurrentAmount(50L)
                .prizeCurrentCurrency("EUR")
                .prizeTotalAmount(300L)
                .prizeTotalCurrency("EUR")
                .build();
    }
}
