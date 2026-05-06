package com.tennisfolio.Tennisfolio.matching.engine;

import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ConstraintChecker {
    public boolean isValid(
            MatchCandidate c,
            List<GamePlayer> allPlayers,
            Set<GamePlayer> used,
            int courtCount,
            int maxGames
    ) {
        GamePlayer p1 = c.teamA.get(0);
        GamePlayer p2 = c.teamA.get(1);
        GamePlayer p3 = c.teamB.get(0);
        GamePlayer p4 = c.teamB.get(1);

        if (hasDuplicatePlayer(p1, p2, p3, p4)) return false;

        if (allPlayers.size() >= courtCount * 4) {
            if (used.contains(p1) || used.contains(p2) || used.contains(p3) || used.contains(p4)) return false;
        }

        if (p1.totalGames + 1 > maxGames) return false;
        if (p2.totalGames + 1 > maxGames) return false;
        if (p3.totalGames + 1 > maxGames) return false;
        if (p4.totalGames + 1 > maxGames) return false;

        int maxAfter = Integer.MIN_VALUE;
        int minAfter = Integer.MAX_VALUE;

        for (GamePlayer p : allPlayers) {
            int after = p.totalGames + (isSelected(p, p1, p2, p3, p4) ? 1 : 0);

            maxAfter = Math.max(maxAfter, after);
            minAfter = Math.min(minAfter, after);
        }

        return maxAfter - minAfter <= 1;
    }

    private boolean hasDuplicatePlayer(GamePlayer p1, GamePlayer p2, GamePlayer p3, GamePlayer p4) {
        return p1 == p2 || p1 == p3 || p1 == p4 || p2 == p3 || p2 == p4 || p3 == p4;
    }

    private boolean isSelected(GamePlayer player, GamePlayer p1, GamePlayer p2, GamePlayer p3, GamePlayer p4) {
        return player == p1 || player == p2 || player == p3 || player == p4;
    }
}
