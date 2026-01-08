package com.tennisfolio.Tennisfolio.infrastructure.api.player.teamDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import com.tennisfolio.Tennisfolio.player.dto.CountryDTO;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import org.springframework.stereotype.Component;

@Component
public class TeamDetailsResponseParser implements ResponseParser<TeamDetailsApiDTO> {
    private final ObjectMapper objectMapper;

    public TeamDetailsResponseParser(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    // exception 정리

    @Override
    public TeamDetailsApiDTO parse(String response) {

        try{

            JsonNode root = objectMapper.readTree(response);
            JsonNode teamNode = root.path("team");

            TeamDetailsApiDTO dto = extractTeamDetails(teamNode);
            dto.setPlayerName(teamNode.path("fullName").asText());
            dto.setPlayerRapidId(teamNode.path("id").asText());

            return dto;

        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }
    }

    private TeamDetailsApiDTO extractTeamDetails(JsonNode teamNode) throws JsonProcessingException {
        JsonNode playerNode = teamNode.path("playerTeamInfo");
        JsonNode countryNode = teamNode.path("country");
        String gender = teamNode.path("gender").asText();
        TeamDetailsApiDTO dto;

        if (playerNode == null || playerNode.isMissingNode()) {
            dto = new TeamDetailsApiDTO(); // 복식팀일 경우 playerTeamInfo 없음
        } else {
            dto = objectMapper.treeToValue(playerNode, TeamDetailsApiDTO.class);

            // 생년월일 형식 변환
            dto.setBirthDate(ConversionUtil.timestampToYyyyMMdd(dto.getBirthDate()));

            // 국가 정보 설정
            CountryDTO country = objectMapper.treeToValue(countryNode, CountryDTO.class);
            dto.updateGender(gender);
            dto.setCountry(country);
        }

        return dto;
    }



}
