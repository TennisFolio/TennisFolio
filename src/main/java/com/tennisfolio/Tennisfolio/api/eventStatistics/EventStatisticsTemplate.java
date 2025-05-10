package com.tennisfolio.Tennisfolio.api.eventStatistics;

import com.tennisfolio.Tennisfolio.api.base.*;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.match.domain.Statistic;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventStatisticsTemplate extends AbstractApiTemplate<List<EventsStatisticsDTO>, List<Statistic>> {
    private final ResponseParser<List<EventsStatisticsDTO>> eventsStatisticsResponseParser;
    private final Mapper<List<EventsStatisticsDTO>, List<Statistic>> eventsStatisticsMapper;
    private final SaveStrategy<List<Statistic>> eventsStatisticSaveStrategy;
    public EventStatisticsTemplate(DecompressorUtil decompressorUtil, ResponseParser<List<EventsStatisticsDTO>> eventsStatisticsResponseParser, Mapper<List<EventsStatisticsDTO>, List<Statistic>> eventsStatisticsMapper, SaveStrategy<List<Statistic>> eventsStatisticSaveStrategy) {
        super(decompressorUtil);
        this.eventsStatisticsResponseParser = eventsStatisticsResponseParser;
        this.eventsStatisticsMapper = eventsStatisticsMapper;
        this.eventsStatisticSaveStrategy = eventsStatisticSaveStrategy;
    }

    @Override
    public List<EventsStatisticsDTO> toDTO(String response) {
        return eventsStatisticsResponseParser.parse(response);
    }

    @Override
    public List<Statistic> toEntity(List<EventsStatisticsDTO> dto, Object... params) {
        return eventsStatisticsMapper.map(dto, params);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.EVENTSTATISTICS.getParam(params);
    }

    @Override
    public List<Statistic> saveEntity(List<Statistic> entity) {
        return eventsStatisticSaveStrategy.save(entity);
    }
}
