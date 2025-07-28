package com.tennisfolio.Tennisfolio.player.infrastructure;

import com.tennisfolio.Tennisfolio.player.domain.Country;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.dto.CountryDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
public class CountryEntity {
    @Column(name="COUNTRY_CODE")
    private String countryCode;
    @Column(name="COUNTRY_NAME")
    private String countryName;

    public CountryEntity(CountryDTO dto){
        if(dto != null){
            this.countryCode = dto.getAlpha();
            this.countryName = dto.getName();
        }

    }

    public static CountryEntity fromModel(Country country) {
        CountryEntity countryEntity = new CountryEntity();
        countryEntity.countryCode = country.getCountryCode();
        countryEntity.countryName = country.getCountryName();

        return countryEntity;
    }

    public Country toModel(){
        return Country.builder()
                .countryCode(countryCode)
                .countryName(countryName)
                .build();
    }

}
