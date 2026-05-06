package com.tennisfolio.Tennisfolio.matching.domain;

import java.util.List;

public class MatchCandidate {
    public final MatchType type;
    public final List<GamePlayer> teamA;
    public final List<GamePlayer> teamB;
    private final List<GamePlayer> allPlayers;

    public MatchCandidate(MatchType type, List<GamePlayer> teamA, List<GamePlayer> teamB) {
        this.type = type;
        this.teamA = teamA;
        this.teamB = teamB;
        this.allPlayers = List.of(teamA.get(0), teamA.get(1), teamB.get(0), teamB.get(1));
    }

    public List<GamePlayer> allPlayers() {
        return allPlayers;
    }
}
