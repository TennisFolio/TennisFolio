package com.tennisfolio.Tennisfolio.api.leagueRounds;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.api.leagueSeasons.LeagueSeasonsDTO;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueRoundsResponseParser implements ResponseParser<List<LeagueRoundsDTO>> {
    private final ObjectMapper objectMapper;

    public LeagueRoundsResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<LeagueRoundsDTO> parse(String response) {
        try{
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode roundsNode = rootNode.path("rounds");

            return objectMapper.readValue(
                    roundsNode.traverse(),
                    new TypeReference<List<LeagueRoundsDTO>>() {});

        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }

    }
}
