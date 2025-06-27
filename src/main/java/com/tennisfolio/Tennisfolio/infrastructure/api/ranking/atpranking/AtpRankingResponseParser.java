package com.tennisfolio.Tennisfolio.infrastructure.api.ranking.atpranking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
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
            String yyyyMMdd = ConversionUtil.timestampToYyyyMMdd(updateDate);

            if(dataArrayNode.isEmpty() || dataArrayNode.isMissingNode()){
                throw new ParserException(ExceptionCode.PARSER_ERROR);
            }

            for(JsonNode dataNode : dataArrayNode){
                AtpRankingApiDTO rankingApiDTO = objectMapper.treeToValue(dataNode, AtpRankingApiDTO.class);
                rankingApiDTO.setUpdateTime(yyyyMMdd);
                list.add(rankingApiDTO);
            }
            return list;
        } catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }
    }
}
