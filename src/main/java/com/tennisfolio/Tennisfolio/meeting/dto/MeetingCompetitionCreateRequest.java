package com.tennisfolio.Tennisfolio.meeting.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class MeetingCompetitionCreateRequest {
    private final boolean sameGenderDoublesOnly;
    private final boolean skillBalancedSchedule;

    @JsonCreator
    public MeetingCompetitionCreateRequest(
            @JsonProperty("sameGenderDoublesOnly") Boolean sameGenderDoublesOnly,
            @JsonProperty("skillBalancedSchedule") Boolean skillBalancedSchedule
    ) {
        this.sameGenderDoublesOnly = Boolean.TRUE.equals(sameGenderDoublesOnly);
        this.skillBalancedSchedule = Boolean.TRUE.equals(skillBalancedSchedule);
    }

    public MeetingCompetitionCreateRequest(Boolean sameGenderDoublesOnly) {
        this(sameGenderDoublesOnly, false);
    }

    public static MeetingCompetitionCreateRequest defaults() {
        return new MeetingCompetitionCreateRequest(false, false);
    }
}
