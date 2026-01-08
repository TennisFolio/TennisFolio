package com.tennisfolio.Tennisfolio.infrastructure.api.ranking.wtaRanking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import com.tennisfolio.Tennisfolio.ranking.dto.WtaRankingApiDTO;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WtaRankingResponseParser implements ResponseParser<List<WtaRankingApiDTO>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<WtaRankingApiDTO> parse(String response) {
        try{
            List<WtaRankingApiDTO> list = new ArrayList<>();

            JsonNode rootNode = objectMapper.readTree(response);

            JsonNode dataArrayNode = rootNode.path("rankings");
            JsonNode updateTime = rootNode.path("updatedAtTimestamp");
            String updateDate = updateTime.toString();
            String yyyyMMdd = ConversionUtil.timestampToYyyyMMdd(updateDate);

            if(dataArrayNode.isEmpty() || dataArrayNode.isMissingNode()){
                throw new ParserException(ExceptionCode.PARSER_ERROR);
            }

            for(JsonNode dataNode : dataArrayNode){
                WtaRankingApiDTO atpRankingApiDTO = objectMapper.treeToValue(dataNode, WtaRankingApiDTO.class);
                atpRankingApiDTO.setUpdateTime(yyyyMMdd);
                list.add(atpRankingApiDTO);
            }
            return list;
        } catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }
    }

}
