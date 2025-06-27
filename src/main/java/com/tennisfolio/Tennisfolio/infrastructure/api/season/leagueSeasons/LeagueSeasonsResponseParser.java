package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueSeasonsResponseParser implements ResponseParser<List<LeagueSeasonsDTO>> {
    private final ObjectMapper objectMapper;

    public LeagueSeasonsResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<LeagueSeasonsDTO> parse(String response) {

        try{
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode seasonsNode = rootNode.path("seasons");

            return objectMapper.readValue(
                    seasonsNode.traverse(),
                    new TypeReference<List<LeagueSeasonsDTO>>() {}
            );
        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }
    }
}
