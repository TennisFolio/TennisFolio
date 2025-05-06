package com.tennisfolio.Tennisfolio.api.categoryTournaments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class CategoryTournamentsResponseParser implements ResponseParser<List<CategoryTournamentsDTO>> {

    private final ObjectMapper objectMapper;
    public CategoryTournamentsResponseParser(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }
    @Override
    public List<CategoryTournamentsDTO> parse(String response) {
        List<CategoryTournamentsDTO> list = new ArrayList<>();
        try{
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode groupsNode = rootNode.path("groups");

            for(JsonNode group : groupsNode){
                JsonNode tournaments = group.path("uniqueTournaments");
                for(JsonNode tournament : tournaments){
                    CategoryTournamentsDTO dto = objectMapper.treeToValue(tournament, CategoryTournamentsDTO.class);
                    // 대회가 7, 8월에 진행되는 경우 둘 다 포함되는 경우가 있음.
                    boolean flag = list.stream().anyMatch(exist -> exist.getTournamentRapidId().equals(dto.getTournamentRapidId()));
                    if(!flag) list.add(dto);
                }
            }
            return list;
        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }

    }
}
