package com.tennisfolio.Tennisfolio.player.domain;

import com.tennisfolio.Tennisfolio.player.dto.CountryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor
public class Country {

    private String countryCode;

    private String countryName;

    public Country(CountryDTO dto){
        if(dto != null){
            this.countryCode = dto.getAlpha();
            this.countryName = dto.getName();
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Country)) return false;
        Country c = (Country) o;
        return Objects.equals(countryName, c.countryName) &&
                Objects.equals(countryCode, c.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryName, countryCode);
    }
}
