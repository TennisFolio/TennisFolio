package com.tennisfolio.Tennisfolio.matching.domain;

import java.util.ArrayList;
import java.util.List;

public class MatchCandidate {
    public final MatchType type;
    public final List<GamePlayer> teamA;
    public final List<GamePlayer> teamB;

    public MatchCandidate(MatchType type, List<GamePlayer> teamA, List<GamePlayer> teamB) {
        this.type = type;
        this.teamA = teamA;
        this.teamB = teamB;
    }

    public List<GamePlayer> allPlayers() {
        List<GamePlayer> result = new ArrayList<>();
        result.addAll(teamA);
        result.addAll(teamB);
        return result;
    }
}
