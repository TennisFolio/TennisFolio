package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.player.domain.Player;

public class TournamentFixtures {


    // Tournaments
    public static Tournament rolandGarrosATP() {
        return Tournament.builder()
                .rapidTournamentId("2480")
                .category(CategoryFixtures.atp())
                .tournamentName("Roland Garros")
                .build();
    }

    public static Tournament rolandGarrosWTA() {
        return Tournament.builder()
                .rapidTournamentId("2577")
                .category(CategoryFixtures.wta())
                .tournamentName("Roland Garros")
                .city("Paris")
                .groundType("Clay")
                .build();
    }

    public static Tournament wimbledonATP() {
        return Tournament.builder()
                .rapidTournamentId("2361")
                .category(CategoryFixtures.atp())
                .tournamentName("Wimbledon")
//                .startTimestamp("1750636800")
//                .endTimestamp("1752364800")
                .build();
    }

    public static Tournament wimbledonWTA() {
        return Tournament.builder()
                .rapidTournamentId("2600")
                .category(CategoryFixtures.wta())
                .tournamentName("Wimbledon")
                .city("London")
                .groundType("Grass")
                .build();
    }

    public static Tournament unitedCupSingle(){
        return Tournament.builder()
                .rapidTournamentId("19728")
                .category(CategoryFixtures.unitedCup())
                .tournamentName("United Cup")
                .groundType("Hardcourt outdoor")
                .build();
    }

    public static Tournament laverCup(){
        return Tournament.builder()
                .rapidTournamentId("10524")
                .category(CategoryFixtures.exhibition())
                .tournamentName("Laver Cup")
                .groundType("Hardcourt indoor")
                .build();
    }
}
