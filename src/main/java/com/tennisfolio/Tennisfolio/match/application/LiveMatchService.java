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
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchSummaryResponse;
import com.tennisfolio.Tennisfolio.match.event.MatchFinishedEvent;
import com.tennisfolio.Tennisfolio.match.event.MatchStartTimeChangedEvent;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();
    public LiveMatchService(ApiWorker apiWorker, PlayerProvider playerProvider, StringRedisTemplate redis, MatchRepository matchRepository, ApplicationEventPublisher eventPublisher) {

        this.apiWorker = apiWorker;
        this.playerProvider = playerProvider;
        this.redis = redis;
        this.matchRepository = matchRepository;
        this.eventPublisher = eventPublisher;
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

        // DB 데이터와 실제 시작 시간이 다른 경우 수정
        changeStartTime();

        // 종료된 경기 처리
        finishMatch(liveMatches);


    }

    private void finishMatch(List<LiveMatchResponse> liveMatches) {
        List<String> endedMatchRapidIds = findEndedMatchRapidIds(liveMatches);
        for(String rapidId : endedMatchRapidIds){
            eventPublisher.publishEvent(
                    new MatchFinishedEvent(rapidId)
            );
        }
    }


    @Transactional
    public void finishMatchProc(String endedId){
        matchRepository.findByRapidMatchId(endedId)
                .ifPresent(findMatch -> {
                    if(findMatch.isEnded()) return;
                    Match event = apiWorker.process(RapidApi.EVENT, endedId);
                    matchRepository.updateMatch(event);
        });
    }


    private void changeStartTime(){
        Set<String> existingKeys = redis.keys("live:*");
        for(String key : existingKeys){
            String rapidId = key.split(":")[2];
            eventPublisher.publishEvent(
                    new MatchStartTimeChangedEvent(rapidId)
            );
        }
    }

    @Transactional
    public void changeStartTimeProc(String rapidId){
        // redis 데이터 조회
        String redisJson = redis.opsForValue().get(rapidId);

        LiveMatchResponse liveMatch = deserialize(redisJson);

        if(liveMatch == null) return;

        matchRepository.findByRapidMatchId(rapidId)
                .ifPresent(findMatch -> {
                    if(findMatch.getStartTimestamp().equals(liveMatch.getTime().getStartTime())) return;
                    findMatch.changeStartTime(liveMatch.getTime().getStartTime());
                    matchRepository.save(findMatch);
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
    public List<LiveMatchSummaryResponse> getLiveEventsSummary(){
        return redis.keys("index:rapidId:*").stream()
                .map(key -> redis.opsForValue().get(key))
                .filter(Objects::nonNull) .map(this::deserialize)
                .map(LiveMatchSummaryResponse::new)
                .toList();
    }


}
