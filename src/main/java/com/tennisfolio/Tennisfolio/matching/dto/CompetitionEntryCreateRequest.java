package com.tennisfolio.Tennisfolio.matching.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompetitionEntryCreateRequest {
    private String playerName;
    private String gender;
}
