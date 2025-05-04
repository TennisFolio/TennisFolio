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
    private final ObjectMapper objectMapper;

    public TeamDetailsResponseParser(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    // exception 정리

    @Override
    public TeamDetailsApiDTO parse(String response) {
        TeamDetailsApiDTO team = new TeamDetailsApiDTO();
        try{

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode teamNode = rootNode.path("team");
            JsonNode playerNode = teamNode.path("playerTeamInfo");
            JsonNode countryNode = teamNode.path("country");
            // 이름
            JsonNode name= teamNode.path("fullName");

            // 복식의 경우 없을 수 있음.
            if(playerNode == null || playerNode.isMissingNode()){
                team.setPlayerRapidId(teamNode.path("id").asText());
            }else{
                team = objectMapper.treeToValue(playerNode, TeamDetailsApiDTO.class);
                CountryDTO country = objectMapper.treeToValue(countryNode, CountryDTO.class);
                team.setCountry(country);
                team.setBirthDate(ConversionUtil.timestampToYyyyMMdd(team.getBirthDate()));

                team.setPlayerRapidId(teamNode.path("id").toString());
            }

            team.setPlayerName(name.asText());

        }catch(Exception e){
            e.printStackTrace();
        }


        return team;
    }



}
