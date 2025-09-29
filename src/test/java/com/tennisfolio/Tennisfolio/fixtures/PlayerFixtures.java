package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.player.domain.Player;

public class PlayerFixtures {
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

    public static Player federer(){
        return Player.builder()
                .rapidPlayerId("14342")
                .playerName("Roger Federer")
                .playerNameKr("로저 페더러")
                .build();
    }

    public static Player sinner(){
        return Player.builder()
                .rapidPlayerId("206570")
                .playerName("Jannik Sinner")
                .playerNameKr("야닉 시너")
                .build();
    }
}
