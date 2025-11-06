package com.tennisfolio.Tennisfolio.match.application;

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
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LiveMatchService {

    private final ApiWorker apiWorker;
    private final PlayerProvider playerProvider;
    private final RedisTemplate redis;
    private final MatchRepository matchRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LiveMatchService(ApiWorker apiWorker, PlayerProvider playerProvider, @Qualifier("redisTemplate") RedisTemplate<String, Object> redis, MatchRepository matchRepository) {

        this.apiWorker = apiWorker;
        this.playerProvider = playerProvider;
        this.redis = redis;
        this.matchRepository = matchRepository;
    }

    public List<LiveMatchResponse> getATPLiveEvents() {
        List<LiveMatchResponse> liveMatches = apiWorker.process(RapidApi.LIVEEVENTS);

        return liveMatches.stream()
                .filter(LiveMatchResponse::isAtp)
                .map(p -> {
                    String homePlayerImage = playerProvider.provide(p.getHomePlayer().getPlayerRapidId()).getImage();
                    String awayPlayerImage = playerProvider.provide(p.getAwayPlayer().getPlayerRapidId()).getImage();

                    p.setPlayerImage(homePlayerImage, awayPlayerImage);
                    return p;
                })
                .collect(Collectors.toList());


    }

    public List<LiveMatchResponse> getWTALiveEvents(){

        List<LiveMatchResponse> liveMatches = apiWorker.process(RapidApi.LIVEEVENTS);
        return liveMatches.stream()
                .filter(LiveMatchResponse::isWta)
                .map(p -> {
                    String homePlayerImage = playerProvider.provide(p.getHomePlayer().getPlayerRapidId()).getImage();
                    String awayPlayerImage = playerProvider.provide(p.getAwayPlayer().getPlayerRapidId()).getImage();

                    p.setPlayerImage(homePlayerImage, awayPlayerImage);
                    return p;
                })
                .collect(Collectors.toList());
    }


    public LiveMatchResponse getLiveEvent(String rapidMatchId) {

        List<LiveMatchResponse> liveMatches = apiWorker.process(RapidApi.LIVEEVENTS);

        return liveMatches
                .stream()
                .filter(response -> rapidMatchId.equals(response.getRapidId()))
                .findFirst()
                .map(p ->{
                    String homePlayerImage = playerProvider.provide(p.getHomePlayer().getPlayerRapidId()).getImage();
                    String awayPlayerImage = playerProvider.provide(p.getAwayPlayer().getPlayerRapidId()).getImage();

                    p.setPlayerImage(homePlayerImage, awayPlayerImage);
                    return p;
                })
                .orElseThrow(() -> new LiveMatchNotFoundException(ExceptionCode.NOT_FOUND));
    }

    public void updateLiveMatches(){

        // 라이브 매치 api 호출
        List<LiveMatchResponse> liveMatches = apiWorker.process(RapidApi.LIVEEVENTS);

        for(var liveMatch : liveMatches){
           String category = liveMatch.getSupportedCategory();
           if(category == null) continue;

           String key = "live:"+ category + ":" + liveMatch.getRapidId();
           String homePlayerImage = playerProvider.provide(liveMatch.getHomePlayer().getPlayerRapidId()).getImage();
           String awayPlayerImage = playerProvider.provide(liveMatch.getAwayPlayer().getPlayerRapidId()).getImage();
           liveMatch.setPlayerImage(homePlayerImage, awayPlayerImage);

            redis.opsForValue()
                    .set(key, liveMatch, Duration.ofMinutes(1));
        }

        // 종료된 것으로 예상되는 rapidId
        List<String> endedMatchRapidIds = findEndedMatchRapidIds(liveMatches);

        endedMatchRapidIds.stream()
                .forEach(p ->{

                    matchRepository.findByRapidMatchId(p)
                            .ifPresentOrElse(findMatch -> {
                                if(findMatch.isEnded()) return;

                                Match updateMatch = apiWorker.process(RapidApi.EVENT, p);
                                findMatch.updateFrom(updateMatch);
                                matchRepository.save(findMatch);
                            }, () -> {
                                Match newMatch =  apiWorker.process(RapidApi.EVENT, p);
                                matchRepository.save(newMatch);
                            });
        });

    }

    public List<LiveMatchResponse> getATPLiveEventsByRedis() {
        Set<String> keys = redis.keys("live:atp:*");
        List<LiveMatchResponse> atpMatches = redis.opsForValue().multiGet(keys);

        return atpMatches;
    }

    public List<LiveMatchResponse> getWTALiveEventsByRedis(){
        Set<String> keys = redis.keys("live:wta:*");
        List<LiveMatchResponse> wtaMatches = redis.opsForValue().multiGet(keys);

        return wtaMatches;
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
