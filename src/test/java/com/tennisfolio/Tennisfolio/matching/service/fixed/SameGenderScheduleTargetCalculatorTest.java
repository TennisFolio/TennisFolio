package com.tennisfolio.Tennisfolio.matching.service.fixed;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SameGenderScheduleTargetCalculatorTest {

    private final SameGenderScheduleTargetCalculator calculator = new SameGenderScheduleTargetCalculator();

    @Test
    void calculatesExactFourGamesTargetForSixMenFiveWomenElevenGames() {
        List<SameGenderScheduleTarget> targets = calculator.calculate(6, 5, 11);

        assertFalse(targets.isEmpty());
        SameGenderScheduleTarget target = targets.get(0);
        assertEquals(6, target.maleGames());
        assertEquals(5, target.femaleGames());
        assertEquals(4, target.minGamesPerPlayer());
        assertEquals(4, target.maxGamesPerPlayer());
    }

    @Test
    void calculatesRangeTargetWithoutForcingEveryoneToFourGames() {
        List<SameGenderScheduleTarget> targets = calculator.calculate(6, 5, 10);

        assertFalse(targets.isEmpty());
        SameGenderScheduleTarget target = targets.get(0);
        assertEquals(5, target.maleGames());
        assertEquals(5, target.femaleGames());
        assertEquals(3, target.minGamesPerPlayer());
        assertEquals(4, target.maxGamesPerPlayer());
    }
}
