package com.tennisfolio.Tennisfolio.club.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ClubMemberBulkCreateRequest {
    private final List<ClubMemberCreateRequest> members;

    @JsonCreator
    public ClubMemberBulkCreateRequest(
            @JsonProperty("members") List<ClubMemberCreateRequest> members
    ) {
        this.members = members;
    }
}
