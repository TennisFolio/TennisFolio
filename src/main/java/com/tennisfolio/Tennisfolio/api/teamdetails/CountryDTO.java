package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryDTO {
    @JsonProperty("alpha2")
    private String alpha;
    @JsonProperty("name")
    private String name;

    public CountryDTO(String alpha, String name){
        this.alpha = alpha;
        this.name = name;
    }
}
