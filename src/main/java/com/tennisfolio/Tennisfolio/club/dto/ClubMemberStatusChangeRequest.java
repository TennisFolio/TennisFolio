package com.tennisfolio.Tennisfolio.club.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ClubMemberStatusChangeRequest {

    private final String memberStatus;
    private final LocalDate statusDate;

    @JsonCreator
    public ClubMemberStatusChangeRequest(
            @JsonProperty("memberStatus") String memberStatus,
            @JsonProperty("statusDate") LocalDate statusDate
    ) {
        this.memberStatus = memberStatus;
        this.statusDate = statusDate;
    }

    public ClubMemberStatusChangeRequest(String memberStatus) {
        this(memberStatus, null);
    }
}
