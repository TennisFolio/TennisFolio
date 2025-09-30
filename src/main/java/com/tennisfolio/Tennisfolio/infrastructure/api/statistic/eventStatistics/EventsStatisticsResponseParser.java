package com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventsStatisticsResponseParser implements ResponseParser<List<EventsStatisticsDTO>> {
    private final ObjectMapper objectMapper;

    public EventsStatisticsResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<EventsStatisticsDTO> parse(String response) {
        try{
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode statisticsNode = rootNode.path("statistics");

            return objectMapper.readValue(
                    statisticsNode.traverse(),
                    new TypeReference<List<EventsStatisticsDTO>>() {
                    });

        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }

    }
}
