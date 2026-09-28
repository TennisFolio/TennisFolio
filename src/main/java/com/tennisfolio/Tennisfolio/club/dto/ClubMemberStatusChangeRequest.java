package com.tennisfolio.Tennisfolio.club.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ClubMemberStatusChangeRequest {

    private final String memberStatus;

    @JsonCreator
    public ClubMemberStatusChangeRequest(@JsonProperty("memberStatus") String memberStatus) {
        this.memberStatus = memberStatus;
    }
}
