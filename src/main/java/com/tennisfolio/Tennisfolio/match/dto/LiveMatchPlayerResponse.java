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

    public LiveMatchPlayerResponse(TeamDTO dto, Player player){
        this.playerRapidId = player.getRapidPlayerId().value();
        this.playerName = dto.getName();
        this.playerImage = player.getImage();
        this.playerRanking = dto.getRanking();
        this.playerCountryAlpha = dto.getCountry().getAlpha();
        this.playerCountryName = dto.getCountry().getName();

    }
}
