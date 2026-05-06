package com.tennisfolio.Tennisfolio.matching.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class GameEntryUpdateRequest {
    private List<PlayerRequest> teamA;
    private List<PlayerRequest> teamB;

    @Getter
    @NoArgsConstructor
    public static class PlayerRequest {
        private Long competitionEntryId;
        private Integer position;
    }
}
