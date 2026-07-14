package com.tennisfolio.Tennisfolio.club.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ClubMemberUpdateRequest {
    private final String name;
    private final String gender;
    private final String role;
    private final String skillNote;
    private final String contactMemo;
    private final String memo;

    @JsonCreator
    public ClubMemberUpdateRequest(
            @JsonProperty("name") String name,
            @JsonProperty("gender") String gender,
            @JsonProperty("role") String role,
            @JsonProperty("skillNote") String skillNote,
            @JsonProperty("contactMemo") String contactMemo,
            @JsonProperty("memo") String memo
    ) {
        this.name = name;
        this.gender = gender;
        this.role = role;
        this.skillNote = skillNote;
        this.contactMemo = contactMemo;
        this.memo = memo;
    }
}
