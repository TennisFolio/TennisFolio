package com.tennisfolio.Tennisfolio.matching.engine;

import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkillBalanceCalculatorTest {

    private final SkillBalanceCalculator calculator = new SkillBalanceCalculator();

    @Test
    void calculatesAbsoluteDifferenceBetweenTeamSkillSums() {
        MatchCandidate candidate = new MatchCandidate(
                MatchType.MIXED,
                List.of(player("M3", GamePlayer.Gender.MALE, 3), player("F1", GamePlayer.Gender.FEMALE, 1)),
                List.of(player("M2", GamePlayer.Gender.MALE, 2), player("F2", GamePlayer.Gender.FEMALE, 2))
        );

        assertEquals(0, calculator.teamSkillDifference(candidate));
    }

    private GamePlayer player(String id, GamePlayer.Gender gender, int skillLevel) {
        return new GamePlayer(id, gender, skillLevel);
    }
}
