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
    private String status;

    public static CompetitionEntryResponse from(CompetitionEntry entry) {
        return new CompetitionEntryResponse(
                entry.getId(),
                entry.getPlayerName(),
                entry.getGender().name(),
                entry.getStatus() == null ? CompetitionEntry.EntryStatus.ACTIVE.name() : entry.getStatus().name()
        );
    }
}
