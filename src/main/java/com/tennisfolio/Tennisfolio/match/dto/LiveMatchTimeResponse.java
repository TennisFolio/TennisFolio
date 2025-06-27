package com.tennisfolio.Tennisfolio.match.dto;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.LiveEventsApiDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class LiveMatchTimeResponse {
    private List<String> period = new ArrayList<>();
    private String currentPeriodStartTimestamp;
    private String startTime;

    public LiveMatchTimeResponse(LiveEventsApiDTO dto){
        this.period.add(dto.getTime().getPeriod1());
        this.period.add(dto.getTime().getPeriod2());
        this.period.add(dto.getTime().getPeriod3());
        this.period.add(dto.getTime().getPeriod4());
        this.period.add(dto.getTime().getPeriod5());

        this.currentPeriodStartTimestamp = dto.getTime().getCurrentPeriodStartTimestamp();
        this.startTime = dto.getStartTime();
    }
}
