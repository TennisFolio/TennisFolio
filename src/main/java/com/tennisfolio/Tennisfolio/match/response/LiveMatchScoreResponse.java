package com.tennisfolio.Tennisfolio.match.response;

import com.tennisfolio.Tennisfolio.api.liveEvents.ScoreDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LiveMatchScoreResponse {
    private Long current;
    private Long display;
    private List<Long> periodScore = new ArrayList<>();
    private String point;

    public LiveMatchScoreResponse(ScoreDTO dto){
        this.current = dto.getCurrent();
        this.display = dto.getDisplay();
        this.point = dto.getPoint();
        this.periodScore.addAll(List.of(
                dto.getPeriod1(),
                dto.getPeriod2(),
                dto.getPeriod3(),
                dto.getPeriod4(),
                dto.getPeriod5()
        ));

    }
}
