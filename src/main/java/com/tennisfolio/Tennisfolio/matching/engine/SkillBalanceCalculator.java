package com.tennisfolio.Tennisfolio.matching.engine;

import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import org.springframework.stereotype.Component;

@Component
public class SkillBalanceCalculator {

    public int teamSkillDifference(MatchCandidate candidate) {
        return Math.abs(teamSkillSum(candidate.teamA) - teamSkillSum(candidate.teamB));
    }

    private int teamSkillSum(Iterable<GamePlayer> players) {
        int sum = 0;
        for (GamePlayer player : players) {
            sum += player.skillLevel;
        }
        return sum;
    }
}
