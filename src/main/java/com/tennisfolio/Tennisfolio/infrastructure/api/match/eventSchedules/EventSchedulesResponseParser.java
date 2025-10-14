package com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventSchedulesResponseParser implements ResponseParser<List<EventSchedulesDTO>> {
    private final ObjectMapper objectMapper;

    public EventSchedulesResponseParser() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<EventSchedulesDTO> parse(String response) {
        try{
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode eventsNode = rootNode.path("events");

            return objectMapper.readValue(
                    eventsNode.traverse(),
                    new TypeReference<List<EventSchedulesDTO>>() {
                    });


        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }
    }
}
