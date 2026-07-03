package com.tennisfolio.Tennisfolio.meeting.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class MeetingStatusUpdateRequest {
    private final String status;

    @JsonCreator
    public MeetingStatusUpdateRequest(@JsonProperty("status") String status) {
        this.status = status;
    }
}
