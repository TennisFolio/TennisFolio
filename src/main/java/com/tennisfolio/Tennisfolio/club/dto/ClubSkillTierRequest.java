package com.tennisfolio.Tennisfolio.club.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ClubSkillTierRequest {
    private final Long id;
    private final String name;

    @JsonCreator
    public ClubSkillTierRequest(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name
    ) {
        this.id = id;
        this.name = name;
    }
}
