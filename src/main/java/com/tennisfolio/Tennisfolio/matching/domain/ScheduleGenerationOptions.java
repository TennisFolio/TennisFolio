package com.tennisfolio.Tennisfolio.matching.domain;

public class ScheduleGenerationOptions {
    private final boolean sameGenderDoublesOnly;
    private final boolean skillBalancedSchedule;

    public ScheduleGenerationOptions(boolean sameGenderDoublesOnly, boolean skillBalancedSchedule) {
        this.sameGenderDoublesOnly = sameGenderDoublesOnly;
        this.skillBalancedSchedule = skillBalancedSchedule;
    }

    public static ScheduleGenerationOptions defaults() {
        return new ScheduleGenerationOptions(false, false);
    }

    public boolean isSameGenderDoublesOnly() { return sameGenderDoublesOnly; }
    public boolean isSkillBalancedSchedule() { return skillBalancedSchedule; }
}
