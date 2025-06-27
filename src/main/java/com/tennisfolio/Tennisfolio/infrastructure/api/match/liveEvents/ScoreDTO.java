package com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScoreDTO {
    @JsonProperty("current")
    private Long current;
    @JsonProperty("display")
    private Long display;
    @JsonProperty("period1")
    private Long period1;
    @JsonProperty("period2")
    private Long period2;
    @JsonProperty("period3")
    private Long period3;
    @JsonProperty("period4")
    private Long period4;
    @JsonProperty("period5")
    private Long period5;
    @JsonProperty("point")
    private String point;
    @JsonProperty("period1TieBreak")
    private Long period1TieBreak;
    @JsonProperty("period2TieBreak")
    private Long period2TieBreak;
    @JsonProperty("period3TieBreak")
    private Long period3TieBreak;
    @JsonProperty("period4TieBreak")
    private Long period4TieBreak;
    @JsonProperty("period5TieBreak")
    private Long period5TieBreak;

    public void nullToZero(){
        if(this.period1 == null) this.period1 = 0L;
        if(this.period2 == null) this.period2 = 0L;
        if(this.period3 == null) this.period3 = 0L;
        if(this.period4 == null) this.period4 = 0L;
        if(this.period5 == null) this.period5 = 0L;
        if(this.period1TieBreak == null) this.period1TieBreak = 0L;
        if(this.period2TieBreak == null) this.period2TieBreak = 0L;
        if(this.period3TieBreak == null) this.period3TieBreak = 0L;
        if(this.period4TieBreak == null) this.period4TieBreak = 0L;
        if(this.period5TieBreak == null) this.period5TieBreak = 0L;
    }
}
