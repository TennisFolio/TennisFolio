package com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UniqueTournamentDTO {
    @JsonProperty("id")
    private String rapidId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("slug")
    private String slug;
}
