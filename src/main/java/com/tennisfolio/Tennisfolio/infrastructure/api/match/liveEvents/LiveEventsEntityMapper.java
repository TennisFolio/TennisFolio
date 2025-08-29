package com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LiveEventsEntityMapper implements EntityMapper<List<LiveEventsApiDTO>, List<LiveMatchResponse>> {
    private final EntityAssemble<List<LiveEventsApiDTO>, List<LiveMatchResponse>> entityAssemble;

    public LiveEventsEntityMapper(EntityAssemble<List<LiveEventsApiDTO>, List<LiveMatchResponse>> entityAssemble) {
        this.entityAssemble = entityAssemble;
    }

    @Override
    public List<LiveMatchResponse> map(List<LiveEventsApiDTO> dto, Object... params) {
        return entityAssemble.assemble(dto, params);
    }
}
