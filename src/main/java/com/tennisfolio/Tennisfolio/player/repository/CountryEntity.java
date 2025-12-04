package com.tennisfolio.Tennisfolio.player.repository;

import com.tennisfolio.Tennisfolio.player.domain.Country;
import com.tennisfolio.Tennisfolio.player.dto.CountryDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class CountryEntity {
    @Column(name="COUNTRY_CODE")
    private String countryCode;
    @Column(name="COUNTRY_NAME")
    private String countryName;


    @Builder
    public CountryEntity(String countryCode, String countryName){
        this.countryCode = countryCode;
        this.countryName = countryName;
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
