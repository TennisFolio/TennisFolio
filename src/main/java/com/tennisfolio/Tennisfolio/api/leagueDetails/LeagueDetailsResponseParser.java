package com.tennisfolio.Tennisfolio.api.leagueDetails;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import org.springframework.stereotype.Component;

@Component
public class LeagueDetailsResponseParser implements ResponseParser<LeagueDetailsDTO> {
    private final ObjectMapper objectMapper;

    public LeagueDetailsResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LeagueDetailsDTO parse(String response) {
        try{
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode tournamentNode = rootNode.path("uniqueTournament");

            LeagueDetailsDTO dto = objectMapper.treeToValue(tournamentNode, LeagueDetailsDTO.class);

            return dto;
        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }

    }
}
