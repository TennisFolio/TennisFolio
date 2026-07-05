package com.tennisfolio.Tennisfolio.meeting.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class MeetingCompetitionCreateRequest {
    private final boolean sameGenderDoublesOnly;

    @JsonCreator
    public MeetingCompetitionCreateRequest(
            @JsonProperty("sameGenderDoublesOnly") Boolean sameGenderDoublesOnly
    ) {
        this.sameGenderDoublesOnly = Boolean.TRUE.equals(sameGenderDoublesOnly);
    }

    public static MeetingCompetitionCreateRequest defaults() {
        return new MeetingCompetitionCreateRequest(false);
    }
}
