package com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class LiveEventsResponseParser implements ResponseParser<List<LiveEventsApiDTO>> {
    private final ObjectMapper objectMapper;

    public LiveEventsResponseParser(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }
    @Override
    public List<LiveEventsApiDTO> parse(String response) {
        try{
            List<LiveEventsApiDTO> eventList = new ArrayList<>();
            JsonNode rootNode = objectMapper.readTree(response);

            JsonNode eventsNode = rootNode.path("events");

            for(JsonNode event : eventsNode){
                LiveEventsApiDTO dto = objectMapper.treeToValue(event, LiveEventsApiDTO.class);
                
                // 책임을 DTO 내부로 이동
                dto.convertTime();

                dto.scoreNullToZero();

                eventList.add(dto);

            }

            eventList.sort(Comparator.comparingInt(LiveEventsApiDTO::getTotalRanking));

            return eventList;

        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }

    }

}
