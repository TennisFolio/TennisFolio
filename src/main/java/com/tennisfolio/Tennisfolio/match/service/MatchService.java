package com.tennisfolio.Tennisfolio.match.service;

import com.tennisfolio.Tennisfolio.api.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.match.response.LiveMatchResponse;

import java.util.List;

public interface MatchService {
    List<LiveMatchResponse> getLiveEvents();
}
