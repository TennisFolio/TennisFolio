package com.tennisfolio.Tennisfolio.match.dto;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.TeamDTO;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LiveMatchPlayerResponse {
    private String playerRapidId;
    private String playerName;
    private String playerImage;
    private String playerRanking;
    private String playerCountryAlpha;
    private String playerCountryName;

    public LiveMatchPlayerResponse(TeamDTO dto){
        this.playerRapidId = dto.getRapidPlayerId();
        this.playerName = dto.getName();
        this.playerRanking = dto.getRanking();
        this.playerCountryAlpha = dto.getCountry().getAlpha();
        this.playerCountryName = dto.getCountry().getName();

    }

    public void setPlayerInfo(String playerImage){
        this.playerImage = playerImage;

    }
}
