package com.tennisfolio.Tennisfolio.match.dto;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.ScoreDTO;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
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
                dto.getPeriod1() == null? 0L : dto.getPeriod1(),
                dto.getPeriod2() == null? 0L : dto.getPeriod2(),
                dto.getPeriod3() == null? 0L : dto.getPeriod3(),
                dto.getPeriod4() == null? 0L : dto.getPeriod4(),
                dto.getPeriod5() == null? 0L : dto.getPeriod5()
        ));

    }
}
