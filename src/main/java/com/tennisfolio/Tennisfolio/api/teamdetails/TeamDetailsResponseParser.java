package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TeamDetailsResponseParser implements ResponseParser<TeamDetailsApiDTO> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // transTimeStamp, eurToUsd 정리
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
            team.setBirthDate(transTimeStamp(team.getBirthDate()));
            // 이름
            JsonNode name= teamNode.path("fullName");
            team.setPlayerName(name.asText());
            team.setPlayerRapidId(teamNode.path("id").toString());
            // 상금
            JsonNode prizeNode = playerNode.path("prizeTotalRaw");
            JsonNode curNode = prizeNode.path("currency");

            String cur = curNode.asText();

            if(cur.equals("EUR")){
                Long prizeCurrent = team.getPrizeCurrent() != null? team.getPrizeCurrent() : 0L;
                Long prizeTotal = team.getPrizeTotal() != null? team.getPrizeTotal() : 0L;

                // USD로 저장
                team.setPrizeCurrent(eurToUsd(prizeCurrent));
                team.setPrizeTotal(eurToUsd(prizeTotal));
            }
        }catch(Exception e){
            e.printStackTrace();
        }


        return team;
    }

    public String transTimeStamp(String timeStamp){
        // Unix 타임스탬프 예제 (초 단위)
        long unixTimestamp = Long.parseLong(timeStamp);

        // 타임스탬프를 Instant로 변환
        Instant instant = Instant.ofEpochSecond(unixTimestamp);

        // 대한민국 표준시 (KST)로 ZonedDateTime 변환
        ZonedDateTime kstDateTime = instant.atZone(ZoneId.of("Asia/Seoul"));

        // 포맷 설정 (yyyy-MM-dd HH:mm:ss)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");


        return kstDateTime.format(formatter);
    }

    public Long eurToUsd(Long eur){
        if(eur == 0L || eur == null) return 0L;
        Long usd = Math.round(eur*0.95);
        return usd;
    }
}
