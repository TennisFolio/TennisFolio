package com.tennisfolio.Tennisfolio.match.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.TimeDTO;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Period {
    private String set1;
    private String set2;
    private String set3;
    private String set4;
    private String set5;

    protected Period() {}
    public Period(TimeDTO dto){
        this.set1 = dto.getPeriod1();
        this.set2 = dto.getPeriod2();
        this.set3 = dto.getPeriod3();
        this.set4 = dto.getPeriod4();
        this.set5 = dto.getPeriod5();

    }
}

