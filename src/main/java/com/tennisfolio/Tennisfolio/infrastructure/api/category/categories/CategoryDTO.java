package com.tennisfolio.Tennisfolio.infrastructure.api.category.categories;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryDTO {
    @JsonProperty("id")
    private String rapidId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("slug")
    private String slug;

    public boolean isAtp(){
        return "atp".equals(slug);
    }

    public boolean isWta(){
        return "wta".equals(slug);
    }
}
