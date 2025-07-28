package com.tennisfolio.Tennisfolio.test.response;

import com.tennisfolio.Tennisfolio.player.repository.PlayerEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TestPlayerResponse {
    private Long playerId;
    private String rapidPlayerId;
    private String playerName;
    private String image;
    private String country;

    public TestPlayerResponse(PlayerEntity player){
        this.playerId = player.getPlayerId();
        this.rapidPlayerId = player.getRapidPlayerId();
        this.playerName = player.getPlayerName();
        this.image = player.getImage();
        this.country = player.getCountryEntity().getCountryCode();
    }
}
