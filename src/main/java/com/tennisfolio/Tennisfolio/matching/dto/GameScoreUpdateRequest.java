package com.tennisfolio.Tennisfolio.matching.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GameScoreUpdateRequest {
    private Integer teamAScore;
    private Integer teamBScore;
    private Integer teamATiebreakScore;
    private Integer teamBTiebreakScore;
}
