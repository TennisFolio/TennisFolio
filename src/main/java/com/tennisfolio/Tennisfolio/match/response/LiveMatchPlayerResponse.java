package com.tennisfolio.Tennisfolio.match.response;

import com.tennisfolio.Tennisfolio.api.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.api.liveEvents.TeamDTO;
import com.tennisfolio.Tennisfolio.common.PlayerSide;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LiveMatchPlayerResponse {
    private Long playerId;
    private String playerRapidId;
    private String playerName;
    private String playerImage;
    private String playerRanking;
    private String playerCountryAlpha;
    private String playerCountryName;

    public LiveMatchPlayerResponse(TeamDTO dto, Player player){
        this.playerId = player.getPlayerId();
        this.playerRapidId = player.getRapidPlayerId();
        this.playerName = dto.getName();
        this.playerImage = player.getImage();
        this.playerRanking = dto.getRanking();
        this.playerCountryAlpha = dto.getCountry().getAlpha();
        this.playerCountryName = dto.getCountry().getName();

    }
}
