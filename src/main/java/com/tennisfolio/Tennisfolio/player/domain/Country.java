package com.tennisfolio.Tennisfolio.player.domain;

import com.tennisfolio.Tennisfolio.player.dto.CountryDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
public class Country {
    @Column(name="COUNTRY_CODE")
    private String countryCode;
    @Column(name="COUNTRY_NAME")
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
