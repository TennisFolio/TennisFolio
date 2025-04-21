package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TeamDetailsResponseParser implements ResponseParser<TeamDetailsApiDTO> {
    private final ObjectMapper objectMapper = new ObjectMapper();


    // exception 정리

    @Override
    public TeamDetailsApiDTO parse(String response) {
        TeamDetailsApiDTO team = new TeamDetailsApiDTO();
        try{
            CountryDTO country = new CountryDTO();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode teamNode = rootNode.path("team");
            JsonNode playerNode = teamNode.path("playerTeamInfo");
            JsonNode countryNode = teamNode.path("country");
            team = objectMapper.treeToValue(playerNode, TeamDetailsApiDTO.class);
            country = objectMapper.treeToValue(countryNode, CountryDTO.class);
            team.setCountry(country);
            team.setBirthDate(ConversionUtil.timestampToYyyyMMdd(team.getBirthDate()));
            // 이름
            JsonNode name= teamNode.path("fullName");
            team.setPlayerName(name.asText());
            team.setPlayerRapidId(teamNode.path("id").toString());
            // 상금
            JsonNode prizeNode = playerNode.path("prizeTotalRaw");
            JsonNode curNode = prizeNode.path("currency");

            String cur = curNode.asText();

            Long prizeCurrent = team.getPrizeCurrent() != null? team.getPrizeCurrent() : 0L;
            Long prizeTotal = team.getPrizeTotal() != null? team.getPrizeTotal() : 0L;

            // USD로 저장
            team.setPrizeCurrent(ConversionUtil.eurToUsd(prizeCurrent, cur));
            team.setPrizeTotal(ConversionUtil.eurToUsd(prizeTotal, cur));

        }catch(Exception e){
            e.printStackTrace();
        }


        return team;
    }



}
