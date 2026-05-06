package com.tennisfolio.Tennisfolio.matching.dto;

import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompetitionEntryResponse {
    private Long competitionEntryId;
    private String playerName;
    private String gender;

    public static CompetitionEntryResponse from(CompetitionEntry entry) {
        return new CompetitionEntryResponse(
                entry.getId(),
                entry.getPlayerName(),
                entry.getGender().name()
        );
    }
}
