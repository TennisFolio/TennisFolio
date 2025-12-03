package com.tennisfolio.Tennisfolio.fixtures;

import com.tennisfolio.Tennisfolio.player.domain.Player;

public class PlayerFixtures {
    // Players
    public static Player nadal() {
        return Player.builder()
                .playerId(1L)
                .rapidPlayerId("14486")
                .playerName("Rafael Nadal")
                .playerNameKr("라파엘 나달")
                .build();
    }

    public static Player alcaraz() {
        return Player.builder()
                .playerId(2L)
                .rapidPlayerId("275923")
                .playerName("Alcaraz")
                .playerNameKr("알카라즈")
                .build();
    }

    public static Player federer(){
        return Player.builder()
                .playerId(4L)
                .rapidPlayerId("14342")
                .playerName("Roger Federer")
                .playerNameKr("로저 페더러")
                .build();
    }

    public static Player sinner(){
        return Player.builder()
                .playerId(3L)
                .rapidPlayerId("206570")
                .playerName("Jannik Sinner")
                .playerNameKr("야닉 시너")
                .build();
    }

    public static Player fritz(){
        return Player.builder()
                .rapidPlayerId("136042")
                .playerName("Taylor Fritz")
                .playerNameKr("테일러 프리츠")
                .build();
    }

    public static Player khachanov(){
        return Player.builder()
                .rapidPlayerId("90080")
                .playerName("Karen Khachanov")
                .playerNameKr("카렌 카차노프")
                .build();
    }

    public static Player norrie(){
        return Player.builder()
                .rapidPlayerId("95935")
                .playerName("Cameron Norrie")
                .playerNameKr("캐머런 노리")
                .build();
    }

    public static Player shelton(){
        return Player.builder()
                .rapidPlayerId("385485")
                .playerName("Ben Shelton")
                .playerNameKr("벤 쉘튼")
                .build();
    }

    public static Player cobolli(){
        return Player.builder()
                .rapidPlayerId("273680")
                .playerName("Flavio Cobolli")
                .playerNameKr("플라비오 코볼리")
                .build();
    }

    public static Player djokovic(){
        return Player.builder()
                .rapidPlayerId("14882")
                .playerName("Novak Djokovic")
                .playerNameKr("노박 조코비치")
                .build();
    }


}
