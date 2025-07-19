package com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents;


import com.tennisfolio.Tennisfolio.infrastructure.api.base.DecompressorUtil;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LiveEventsTemplate extends StrategyApiTemplate<List<LiveEventsApiDTO>, Void> {

    public LiveEventsTemplate(
         @Qualifier("liveEventsResponseParser") ResponseParser<List<LiveEventsApiDTO>> parser) {
        super(parser, null,  RapidApi.LIVEEVENTS);
    }

}
