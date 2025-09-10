package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.player.domain.Player;

public class TournamentFixtures {

    // Players
    public static Player nadal() {
        return Player.builder()
                .rapidPlayerId("14486")
                .playerName("Rafael Nadal")
                .playerNameKr("라파엘 나달")
                .build();
    }

    public static Player alcaraz() {
        return Player.builder()
                .rapidPlayerId("275923")
                .playerName("Alcaraz")
                .playerNameKr("알카라즈")
                .build();
    }

    // Categories
    public static Category atp() {
        return Category.builder()
                .rapidCategoryId("3")
                .categoryName("ATP")
                .categorySlug("atp")
                .build();
    }

    public static Category wta() {
        return Category.builder()
                .rapidCategoryId("6")
                .categoryName("WTA")
                .categorySlug("wta")
                .build();
    }

    // Tournaments
    public static Tournament rolandGarrosATP() {
        return Tournament.builder()
                .rapidTournamentId("2480")
                .category(atp())
                .tournamentName("Roland Garros")
                .build();
    }

    public static Tournament rolandGarrosWTA() {
        return Tournament.builder()
                .rapidTournamentId("2577")
                .category(wta())
                .tournamentName("Roland Garros")
                .city("Paris")
                .groundType("Clay")
                .build();
    }

    public static Tournament wimbledonATP() {
        return Tournament.builder()
                .rapidTournamentId("2361")
                .category(atp())
                .tournamentName("Wimbledon")
                .build();
    }

    public static Tournament wimbledonWTA() {
        return Tournament.builder()
                .rapidTournamentId("2600")
                .category(wta())
                .tournamentName("Wimbledon")
                .city("London")
                .groundType("Grass")
                .build();
    }
}
