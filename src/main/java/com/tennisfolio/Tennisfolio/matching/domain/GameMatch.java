package com.tennisfolio.Tennisfolio.matching.domain;

import java.util.List;

public class GameMatch {
    public final int round;
    public final int court;
    public final MatchType type;

    public final List<GamePlayer> teamA;
    public final List<GamePlayer> teamB;

    public GameMatch(int round, int court, MatchType type,
                     List<GamePlayer> teamA, List<GamePlayer> teamB) {
        this.round = round;
        this.court = court;
        this.type = type;
        this.teamA = teamA;
        this.teamB = teamB;
    }

    @Override
    public String toString() {
        return "Round " + round + " Court " + court + " " + type +
                " " + teamA + " vs " + teamB;
    }
}
