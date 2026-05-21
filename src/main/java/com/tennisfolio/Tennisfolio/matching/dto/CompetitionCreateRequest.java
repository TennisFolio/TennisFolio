package com.tennisfolio.Tennisfolio.matching.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompetitionCreateRequest {
    private String mode;
    private String competitionName;
    private int maleCount;
    private int femaleCount;
    private int courtCount;
    private int hours;
    private Long seed;
}
