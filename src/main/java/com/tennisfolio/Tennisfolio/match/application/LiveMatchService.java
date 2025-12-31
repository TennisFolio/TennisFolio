package com.tennisfolio.Tennisfolio.match.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.aop.SkipLog;
import com.tennisfolio.Tennisfolio.exception.LiveMatchNotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.redis.core.ScanOptions.scanOptions;

@Slf4j
@Service
public class LiveMatchService {

    private final ApiWorker apiWorker;
    private final PlayerProvider playerProvider;
    private final StringRedisTemplate redis;
    private final MatchRepository matchRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    public LiveMatchService(ApiWorker apiWorker, PlayerProvider playerProvider, StringRedisTemplate redis, MatchRepository matchRepository) {

        this.apiWorker = apiWorker;
        this.playerProvider = playerProvider;
        this.redis = redis;
        this.matchRepository = matchRepository;
    }



    @Transactional
    public void updateLiveMatches(){

        // 라이브 매치 api 호출
        List<LiveMatchResponse> liveMatches = apiWorker.process(RapidApi.LIVEEVENTS);

        for(var liveMatch : liveMatches){
           String category = liveMatch.getSupportedCategory();
           if(category == null) continue;

           String key = "live:"+ category + ":" + liveMatch.getRapidId();
           String indexKey = "index:rapidId:" + liveMatch.getRapidId();

           String homePlayerImage = playerProvider.provide(liveMatch.getHomePlayer().getPlayerRapidId()).getImage();
           String awayPlayerImage = playerProvider.provide(liveMatch.getAwayPlayer().getPlayerRapidId()).getImage();
           liveMatch.setPlayerImage(homePlayerImage, awayPlayerImage);
           try{
               String liveMatchJson = objectMapper.writeValueAsString(liveMatch);
               redis.opsForValue().set(key, liveMatchJson, Duration.ofMinutes(1));
               redis.opsForValue().set(indexKey, liveMatchJson, Duration.ofMinutes(1));

           }catch(Exception e){
               e.printStackTrace();
           }

        }

        // 종료된 것으로 예상되는 rapidId
        List<String> endedMatchRapidIds = findEndedMatchRapidIds(liveMatches);

        updateEndedMatches(endedMatchRapidIds);

    }

    public void updateEndedMatches(List<String> endedIds){
        endedIds.stream()
                .forEach(p ->{

                    matchRepository.findByRapidMatchId(p)
                            .ifPresent(findMatch -> {
                                if(findMatch.isEnded()) return;
                                Match event = apiWorker.process(RapidApi.EVENT, p);
                                matchRepository.updateMatch(event);
                            });
                });
    }

    public List<LiveMatchResponse> getATPLiveEventsByRedis() {
        Set<String> keys = redis.keys("live:atp:*");

        return getLiveMatchResponses(keys);

    }


    public List<LiveMatchResponse> getWTALiveEventsByRedis(){
        Set<String> keys = redis.keys("live:wta:*");

        return getLiveMatchResponses(keys);
    }

    private List<LiveMatchResponse> getLiveMatchResponses(Set<String> keys) {
        if (keys == null || keys.isEmpty()) return List.of();

        return redis.opsForValue().multiGet(keys).stream()
                .map(this::deserialize)
                .filter(Objects::nonNull)
                .toList();
    }



    public List<LiveMatchResponse> getEtcLiveEventsByRedis() {
        return redis.keys("live:*").stream()
                .map(key -> redis.opsForValue().get(key))
                .filter(Objects::nonNull)
                .map(this::deserialize)
                .filter(Objects::nonNull)
                .filter(LiveMatchResponse::isSupportedCategory)
                .filter(match -> !match.isAtp() && !match.isWta())
                .toList();
    }

    private LiveMatchResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, LiveMatchResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize LiveMatchResponse", e);
            return null;
        }
    }
    public List<LiveMatchResponse> getAllLiveEventsByRedis(){
        List<LiveMatchResponse> atp = Optional.ofNullable(getATPLiveEventsByRedis()).orElseGet(List::of);
        List<LiveMatchResponse> wta = Optional.ofNullable(getWTALiveEventsByRedis()).orElseGet(List::of);

        return Stream.concat(atp.stream(), wta.stream())
                .collect(Collectors.toList());
    }

    public LiveMatchResponse getLiveEventByRedis(String rapidId){
        String indexKey = "index:rapidId:" + rapidId;
        String json = redis.opsForValue().get(indexKey);
        return deserialize(json);
    }

    private List<String> findEndedMatchRapidIds(List<LiveMatchResponse> liveMatches){
        List<String> newMatchIds = liveMatches.stream()
                .filter(LiveMatchResponse::isSupportedCategory)
                .map(LiveMatchResponse::getRapidId)
                .toList();

        Set<String> existingKeys = redis.keys("live:*:*");

        List<String> existingIds = existingKeys.stream()
                .map(key -> key.split(":")[2])
                .toList();

        return existingIds.stream()
                .filter(id -> !newMatchIds.contains(id))
                .toList();

    }


}
