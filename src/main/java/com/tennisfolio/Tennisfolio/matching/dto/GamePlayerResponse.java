package com.tennisfolio.Tennisfolio.matching.dto;

import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GamePlayerResponse {
    private Long competitionEntryId;
    private String playerName;
    private String gender;
    private Integer position;

    public static GamePlayerResponse from(GameEntry gameEntry) {
        CompetitionEntry competitionEntry = gameEntry.getCompetitionEntry();
        return new GamePlayerResponse(
                competitionEntry.getId(),
                competitionEntry.getPlayerName(),
                competitionEntry.getGender().name(),
                gameEntry.getPosition()
        );
    }
}
