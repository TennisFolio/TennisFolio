package com.tennisfolio.Tennisfolio.matching.service.fixed;

public record SameGenderScheduleTarget(
        int maleGames,
        int femaleGames,
        int minGamesPerPlayer,
        int maxGamesPerPlayer
) {
}
