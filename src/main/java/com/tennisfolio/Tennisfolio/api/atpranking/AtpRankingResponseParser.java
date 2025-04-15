package com.tennisfolio.Tennisfolio.api.atpranking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class AtpRankingResponseParser implements ResponseParser<List<AtpRankingApiDTO>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<AtpRankingApiDTO> parse(String response) {
        try{
            List<AtpRankingApiDTO> list = new ArrayList<>();

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataArrayNode = rootNode.path("rankings");
            JsonNode updateTime = rootNode.path("updatedAtTimestamp");
            String updateDate = updateTime.toString();
            for(JsonNode dataNode : dataArrayNode){
                AtpRankingApiDTO rankingApiDTO = objectMapper.treeToValue(dataNode, AtpRankingApiDTO.class);
                String id = dataNode.path("team").path("id").toString();
                rankingApiDTO.setUpdateTime(updateDate);
                list.add(rankingApiDTO);
            }
            return list;
        } catch(Exception e){
            throw new RuntimeException("Parsing error", e);
        }
    }
}
