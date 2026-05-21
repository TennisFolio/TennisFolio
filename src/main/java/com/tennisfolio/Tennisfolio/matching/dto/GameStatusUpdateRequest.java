package com.tennisfolio.Tennisfolio.matching.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameStatusUpdateRequest {
    private String status;
    private Integer teamAScore;
    private Integer teamBScore;
}
