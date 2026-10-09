package com.tennisfolio.Tennisfolio.club.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ClubMemberCreateRequest {
    private final String name;
    private final String gender;
    private final Long skillTierId;
    private final String contactMemo;
    private final String memo;
    private final LocalDate joinedAt;

    @JsonCreator
    public ClubMemberCreateRequest(
            @JsonProperty("name") String name,
            @JsonProperty("gender") String gender,
            @JsonProperty("skillTierId") Long skillTierId,
            @JsonProperty("contactMemo") String contactMemo,
            @JsonProperty("memo") String memo,
            @JsonProperty("joinedAt") LocalDate joinedAt
    ) {
        this.name = name;
        this.gender = gender;
        this.skillTierId = skillTierId;
        this.contactMemo = contactMemo;
        this.memo = memo;
        this.joinedAt = joinedAt;
    }
}
