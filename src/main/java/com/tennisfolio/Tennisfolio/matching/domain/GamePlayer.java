package com.tennisfolio.Tennisfolio.matching.domain;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class GamePlayer {
    public enum Gender { MALE, FEMALE }

    public final String id;
    public final Gender gender;

    public int totalGames;
    public int mixedGames;
    public int maleGames;
    public int femaleGames;
    public int randomGames;
    public int consecutiveRounds;

    public Map<String, Integer> partnerCount = new HashMap<>();
    public Map<String, Integer> opponentCount = new HashMap<>();
    public Map<MatchType, Integer> typeExperience = new EnumMap<>(MatchType.class);

    public GamePlayer(String id, Gender gender) {
        this.id = id;
        this.gender = gender;

        for(MatchType t :MatchType.values()){
            typeExperience.put(t, 0);
        }
    }

    @Override
    public String toString() {
        return id;
    }
}
