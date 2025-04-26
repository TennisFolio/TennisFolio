package com.tennisfolio.Tennisfolio.api.liveEvents;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.base.DecompressorUtil;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LiveEventsTemplate extends AbstractApiTemplate<List<LiveEventsApiDTO>, Void> {
    private final LiveEventsResponseParser parser;
    public LiveEventsTemplate(DecompressorUtil decompressorUtil, LiveEventsResponseParser parser) {
        super(decompressorUtil);
        this.parser = parser;
    }

    @Override
    public List<LiveEventsApiDTO> toDTO(String response) {
        return parser.parse(response);
    }

    @Override
    public Void toEntity(List<LiveEventsApiDTO> dto) {
        return null;
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.LIVEEVENTS.getParam(params);
    }
}
