package com.tennisfolio.Tennisfolio.api.liveEvents;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.base.DecompressorUtil;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LiveEventsTemplate extends AbstractApiTemplate<List<LiveEventsApiDTO>, Void> {

    private final ResponseParser<List<LiveEventsApiDTO>> parser;
    public LiveEventsTemplate(DecompressorUtil decompressorUtil,
                              @Qualifier("liveEventsResponseParser") LiveEventsResponseParser parser) {
        super(decompressorUtil);
        this.parser = parser;
    }

    @Override
    public List<LiveEventsApiDTO> toDTO(String response) {
        return parser.parse(response);
    }

    @Override
    public Void toEntity(List<LiveEventsApiDTO> dto, Object... params) {
        return null;
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.LIVEEVENTS.getParam(params);
    }

    @Override
    public Void saveEntity(Void entity) {
        return null;
    }
}
