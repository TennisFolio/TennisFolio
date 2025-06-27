package com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueEventsByRoundResponseParser implements ResponseParser<List<LeagueEventsByRoundDTO>> {
    private final ObjectMapper objectMapper;

    public LeagueEventsByRoundResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<LeagueEventsByRoundDTO> parse(String response) {
        try{
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode eventsNode = rootNode.path("events");

            return objectMapper.readValue(
                    eventsNode.traverse(),
                    new TypeReference<List<LeagueEventsByRoundDTO>>() {
                    });

        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }

    }
}
