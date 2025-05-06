package com.tennisfolio.Tennisfolio.api.categoryTournaments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.api.categories.CategoryDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryTournamentsDTO {
    @JsonProperty("id")
    private String tournamentRapidId;
    @JsonProperty("name")
    private String tournamentName;
    @JsonProperty("category")
    private CategoryDTO category;
}
