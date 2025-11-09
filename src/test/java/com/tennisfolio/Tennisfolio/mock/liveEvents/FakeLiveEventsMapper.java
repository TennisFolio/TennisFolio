package com.tennisfolio.Tennisfolio.mock.liveEvents;

import com.tennisfolio.Tennisfolio.fixtures.LiveEventsFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;

import java.util.List;

public class FakeLiveEventsMapper implements EntityMapper<List<LiveEventsApiDTO>, List<LiveMatchResponse>> {
    @Override
    public List<LiveMatchResponse> map(List<LiveEventsApiDTO> dto, Object... params) {
        return List.of(LiveEventsFixtures.liveMatchInProgress1());
    }
}
