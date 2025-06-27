package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UniqueTournamentDTO {
    @JsonProperty("category")
    private CategoryDTO category;
    @JsonProperty("id")
    private String rapidId;
}
