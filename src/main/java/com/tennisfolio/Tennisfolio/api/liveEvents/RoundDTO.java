package com.tennisfolio.Tennisfolio.api.liveEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoundDTO {
    @JsonProperty("round")
    private Long round;
    @JsonProperty("name")
    private String name;
    @JsonProperty("slug")
    private String slug;

}
