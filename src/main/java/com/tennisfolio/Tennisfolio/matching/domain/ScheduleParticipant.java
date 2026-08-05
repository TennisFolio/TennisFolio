package com.tennisfolio.Tennisfolio.matching.domain;

public class ScheduleParticipant {
    private final String id;
    private final GamePlayer.Gender gender;
    private final Integer skillLevel;

    public ScheduleParticipant(String id, GamePlayer.Gender gender, Integer skillLevel) {
        this.id = id;
        this.gender = gender;
        this.skillLevel = skillLevel;
    }

    public String getId() { return id; }
    public GamePlayer.Gender getGender() { return gender; }
    public Integer getSkillLevel() { return skillLevel; }
}
