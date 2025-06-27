package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import org.springframework.stereotype.Component;

@Component
public class LeagueSeasonInfoResponseParser implements ResponseParser<LeagueSeasonInfoDTO> {
    private final ObjectMapper objectMapper;

    public LeagueSeasonInfoResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LeagueSeasonInfoDTO parse(String response) {
        try{
            JsonNode rootNode = objectMapper.readTree(response);

            JsonNode infoNode = rootNode.path("info");

            return objectMapper.treeToValue(infoNode, LeagueSeasonInfoDTO.class);

        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }

    }
}
