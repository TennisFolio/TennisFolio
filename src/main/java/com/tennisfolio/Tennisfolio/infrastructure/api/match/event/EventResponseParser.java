package com.tennisfolio.Tennisfolio.infrastructure.api.match.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules.EventSchedulesDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventResponseParser implements ResponseParser<EventDTO> {
    private final ObjectMapper objectMapper;

    public EventResponseParser() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public EventDTO parse(String response) {
        try{
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode eventNode = rootNode.path("event");

            return objectMapper.treeToValue(eventNode, EventDTO.class);

        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }
    }
}
