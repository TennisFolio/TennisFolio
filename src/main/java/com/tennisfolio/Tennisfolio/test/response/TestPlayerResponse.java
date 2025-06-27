package com.tennisfolio.Tennisfolio.test.response;

import com.tennisfolio.Tennisfolio.player.domain.Player;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class TestPlayerResponse {
    private Long playerId;
    private String rapidPlayerId;
    private String playerName;
    private String image;
    private String country;

    public TestPlayerResponse(Player player){
        this.playerId = player.getPlayerId();
        this.rapidPlayerId = player.getRapidPlayerId();
        this.playerName = player.getPlayerName();
        this.image = player.getImage();
        this.country = player.getCountry().getCountryCode();
    }
}
