package com.tennisfolio.Tennisfolio.match.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.TimeDTO;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;

@Embeddable
@Getter
@Builder
public class Period {
    private String set1;
    private String set2;
    private String set3;
    private String set4;
    private String set5;

    protected Period() {}
    public Period(String set1, String set2, String set3, String set4, String set5){
        this.set1 = set1;
        this.set2 = set2;
        this.set3 = set3;
        this.set4 = set4;
        this.set5 = set5;

    }
}

