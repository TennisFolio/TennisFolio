package com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules.UniqueTournamentDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentDTO {
    @JsonProperty("uniqueTournament")
    private UniqueTournamentDTO uniqueTournamentDTO;
    @JsonProperty("id")
    private String rapidId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("category")
    private CategoryDTO category;


    @JsonProperty("id")
    public String getRapidId() {
        return uniqueTournamentDTO != null ? uniqueTournamentDTO.getRapidId() : null;
    }

}
