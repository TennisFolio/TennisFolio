package com.tennisfolio.Tennisfolio.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameEntryUpdateResponse {
    private GameResponse game;
    private CompetitionStatResponse stat;
    private GameBalanceResponse balance;
}
