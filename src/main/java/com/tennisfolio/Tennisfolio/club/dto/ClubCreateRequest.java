package com.tennisfolio.Tennisfolio.club.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ClubCreateRequest {
    private final String name;
    private final String description;

    @JsonCreator
    public ClubCreateRequest(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description
    ) {
        this.name = name;
        this.description = description;
    }
}
