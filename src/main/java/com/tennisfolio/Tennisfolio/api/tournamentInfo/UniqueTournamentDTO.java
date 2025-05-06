package com.tennisfolio.Tennisfolio.api.tournamentInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.api.categories.CategoryDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UniqueTournamentDTO {
    @JsonProperty("category")
    private CategoryDTO category;
    @JsonProperty("id")
    private String rapidId;
}
