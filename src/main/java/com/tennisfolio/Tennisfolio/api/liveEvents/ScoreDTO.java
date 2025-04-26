package com.tennisfolio.Tennisfolio.api.liveEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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

    public void nullToZero(){
        if(this.period1 == null) this.period1 = 0L;
        if(this.period2 == null) this.period2 = 0L;
        if(this.period3 == null) this.period3 = 0L;
        if(this.period4 == null) this.period4 = 0L;
        if(this.period5 == null) this.period5 = 0L;
    }
}
