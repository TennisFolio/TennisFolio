package com.tennisfolio.Tennisfolio.matching.engine;

import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
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

        List<GamePlayer> selected = c.allPlayers();

        if (new HashSet<>(selected).size() != selected.size()) return false;

        if (allPlayers.size() >= courtCount * 4) {
            for (GamePlayer p : selected) {
                if (used.contains(p)) return false;
            }
        }

        for (GamePlayer p : selected) {
            if (p.totalGames + 1 > maxGames) return false;
        }

        // 🔥 선택 후 편차 체크
        int maxAfter = Integer.MIN_VALUE;
        int minAfter = Integer.MAX_VALUE;

        Set<GamePlayer> selectedSet = new HashSet<>(selected);

        for (GamePlayer p : allPlayers) {
            int after = p.totalGames + (selectedSet.contains(p) ? 1 : 0);

            maxAfter = Math.max(maxAfter, after);
            minAfter = Math.min(minAfter, after);
        }

        return maxAfter - minAfter <= 1;
    }
}
