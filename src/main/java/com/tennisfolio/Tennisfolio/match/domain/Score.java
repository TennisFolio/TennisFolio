package com.tennisfolio.Tennisfolio.match.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.ScoreDTO;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;

@Embeddable
@Getter
@Builder
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
    public Score(Long set1, Long set2, Long set3, Long set4, Long set5, Long set1Tie, Long set2Tie, Long set3Tie, Long set4Tie, Long set5Tie){
        this.set1 = set1;
        this.set2 = set2;
        this.set3 = set3;
        this.set4 = set4;
        this.set5 = set5;
        this.set1Tie = set1Tie;
        this.set2Tie = set2Tie;
        this.set3Tie = set3Tie;
        this.set4Tie = set4Tie;
        this.set5Tie = set5Tie;

    }

    public static Score from(ScoreDTO scoreDTO){
        return Score.builder()
                .set1(scoreDTO.getPeriod1())
                .set2(scoreDTO.getPeriod2())
                .set3(scoreDTO.getPeriod3())
                .set4(scoreDTO.getPeriod4())
                .set5(scoreDTO.getPeriod5())
                .set1Tie(scoreDTO.getPeriod1TieBreak())
                .set2Tie(scoreDTO.getPeriod2TieBreak())
                .set3Tie(scoreDTO.getPeriod3TieBreak())
                .set4Tie(scoreDTO.getPeriod4TieBreak())
                .set5Tie(scoreDTO.getPeriod5TieBreak())
                .build();
    }
    public void nullToZero(){
        if(this.set1 == null) this.set1 = 0L;
        if(this.set2 == null) this.set2 = 0L;
        if(this.set3 == null) this.set3 = 0L;
        if(this.set4 == null) this.set4 = 0L;
        if(this.set5 == null) this.set5 = 0L;
        if(this.set1Tie == null) this.set1Tie = 0L;
        if(this.set2Tie == null) this.set2Tie = 0L;
        if(this.set3Tie == null) this.set3Tie = 0L;
        if(this.set4Tie == null) this.set4Tie = 0L;
        if(this.set5Tie == null) this.set5Tie = 0L;
    }
}
