package com.tennisfolio.Tennisfolio.api.tournamentInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.api.categoryTournaments.CategoryTournamentsDTO;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoResponseParser implements ResponseParser<TournamentInfoDTO> {
    private final ObjectMapper objectMapper;

    public TournamentInfoResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public TournamentInfoDTO parse(String response) {
        try{
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode metaNode = rootNode.path("meta");

            TournamentInfoDTO dto = objectMapper.treeToValue(metaNode, TournamentInfoDTO.class);

            return dto;
        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }

    }
}
