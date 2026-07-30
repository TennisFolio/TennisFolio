package com.tennisfolio.Tennisfolio.club.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ClubCreateRequest {
    private final String name;
    private final String description;
    private final List<ClubSkillTierRequest> skillTiers;

    @JsonCreator
    public ClubCreateRequest(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("skillTiers") List<ClubSkillTierRequest> skillTiers
    ) {
        this.name = name;
        this.description = description;
        this.skillTiers = skillTiers == null ? List.of() : List.copyOf(skillTiers);
    }

    public ClubCreateRequest(String name, String description) {
        this(name, description, List.of());
    }
}
