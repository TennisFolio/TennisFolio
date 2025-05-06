package com.tennisfolio.Tennisfolio.api.leagueSeasonInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
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
