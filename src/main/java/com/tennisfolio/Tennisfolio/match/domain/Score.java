package com.tennisfolio.Tennisfolio.match.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.ScoreDTO;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Score {
    private Long set1;
    private Long set2;
    private Long set3;
    private Long set4;
    private Long set5;
    private Long set1Tie;
    private Long set2Tie;
    private Long set3Tie;
    private Long set4Tie;
    private Long set5Tie;

    protected Score() {}
    public Score(ScoreDTO dto){
        this.set1 = dto.getPeriod1();
        this.set2 = dto.getPeriod2();
        this.set3 = dto.getPeriod3();
        this.set4 = dto.getPeriod4();
        this.set5 = dto.getPeriod5();
        this.set1Tie = dto.getPeriod1TieBreak();
        this.set2Tie = dto.getPeriod2TieBreak();
        this.set3Tie = dto.getPeriod3TieBreak();
        this.set4Tie = dto.getPeriod4TieBreak();
        this.set5Tie = dto.getPeriod5TieBreak();

    }
}
