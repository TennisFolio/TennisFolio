package com.tennisfolio.Tennisfolio.matching.domain;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class GamePlayer {
    public enum Gender { MALE, FEMALE }

    public final String id;
    public final Gender gender;
    public final int skillLevel;
    public Long competitionEntryId;

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
        this(id, gender, 0);
    }

    public GamePlayer(String id, Gender gender, int skillLevel) {
        this(id, gender, skillLevel, null);
    }

    public GamePlayer(String id, Gender gender, int skillLevel, Long competitionEntryId) {
        this.id = id;
        this.gender = gender;
        this.skillLevel = skillLevel;
        this.competitionEntryId = competitionEntryId;

        for(MatchType t :MatchType.values()){
            typeExperience.put(t, 0);
        }
    }

    @Override
    public String toString() {
        return id;
    }
}
