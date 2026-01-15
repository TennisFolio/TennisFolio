package com.tennisfolio.Tennisfolio.match.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.category.enums.CategoryType;
import com.tennisfolio.Tennisfolio.fixtures.EtcLiveEventsFixtures;
import com.tennisfolio.Tennisfolio.fixtures.LiveEventsFixtures;
import com.tennisfolio.Tennisfolio.fixtures.MatchFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.event.EventTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.LiveEventsTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.match.application.LiveMatchService;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchSummaryResponse;
import com.tennisfolio.Tennisfolio.match.event.MatchFinishedEvent;
import com.tennisfolio.Tennisfolio.match.event.MatchStartTimeChangedEvent;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakeMatchRepository;
import com.tennisfolio.Tennisfolio.mock.FakeRedisTemplate;
import com.tennisfolio.Tennisfolio.mock.event.FakeEventMapper;
import com.tennisfolio.Tennisfolio.mock.liveEvents.FakeLiveEventsMapperSecond;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class LiveMatchServiceTest {
    LiveMatchService liveMatchService;
    ApiWorker apiWorker;
    @Mock
    PlayerProvider playerProvider;

    ObjectMapper objectMapper = new ObjectMapper();

    StringRedisTemplate redisTemplate = new FakeRedisTemplate();
    MatchRepository matchRepository = new FakeMatchRepository();

    private List<StrategyApiTemplate<?, ?>> strategies = new ArrayList<>();
    private ApiCaller fakeApiCaller = new FakeApiCaller();
    @Mock
    private ResponseParser parser;
    @Mock
    private ApiCallCounter apiCallCounter;

    @Mock
    private RedisRateLimiter redisRateLimiter;

    @Mock
    PlayerImageService playerImageService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Captor
    ArgumentCaptor<Object> eventCaptor;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);


        FakeLiveEventsMapperSecond fakeLiveEventsMapperSecond = new FakeLiveEventsMapperSecond();
        FakeEventMapper fakeEventMapper = new FakeEventMapper();
        LiveEventsTemplate liveEventsTemplateSecond = new LiveEventsTemplate(fakeApiCaller, parser, fakeLiveEventsMapperSecond, apiCallCounter);
        EventTemplate eventTemplate = new EventTemplate(fakeApiCaller, parser, fakeEventMapper, apiCallCounter);

        strategies.addAll(List.of(liveEventsTemplateSecond, eventTemplate));
        when(playerProvider.provide("206570")).thenReturn(Player.builder().image("player/206570").build());
        when(playerProvider.provide("275923")).thenReturn(Player.builder().image("player/275923").build());
        apiWorker = new ApiWorker(strategies, redisRateLimiter);

        liveMatchService = new LiveMatchService(apiWorker, playerProvider, redisTemplate, matchRepository, eventPublisher);
    }

    @Test
    void 처음_경기_생성시_redis_데이터_추가(){

        liveMatchService.updateLiveMatches();
        LiveMatchResponse res = null;
        try{
            res = objectMapper.readValue(redisTemplate.opsForValue().get("live:atp:1"), LiveMatchResponse.class);
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }
        assertThat(res.getHomeScore().getPoint()).isEqualTo("40");
        assertThat(res.getAwayScore().getPoint()).isEqualTo("40");

    }

    @Test
    void 데이터_변경_시_redis_데이터_수정(){
        LiveMatchResponse res = null;
        try{
            // 첫번째 호출로 progress1 수정
            // redis 수정 -> redis 조회
            LiveMatchResponse response1 = LiveEventsFixtures.liveMatchInProgress1();
            String responseStr = objectMapper.writeValueAsString(response1);
            redisTemplate.opsForValue().set("live:atp:1", responseStr);
            // 두번째 호출로 progress2로 수정
            // redis 수정 -> redis 조회
            liveMatchService.updateLiveMatches();

            res = objectMapper.readValue(redisTemplate.opsForValue().get("live:atp:1"), LiveMatchResponse.class);
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }

        assertThat(res.getHomeScore().getPoint()).isEqualTo("40");
        assertThat(res.getAwayScore().getPoint()).isEqualTo("40");

    }

    @Test
    void 경기_종료_시_데이터_업데이트(){
        try{
            // 1. redis에 1,2번 경기 데이터 추가
            LiveMatchResponse match1 = LiveEventsFixtures.liveMatchInProgress1();
            LiveMatchResponse match3 = LiveEventsFixtures.liveMatchInProgress3();

            String match1Str = objectMapper.writeValueAsString(match1);
            String match3Str = objectMapper.writeValueAsString(match3);
            matchRepository.save(MatchFixtures.wimbledonMen2025FinalMatch());
            matchRepository.save(MatchFixtures.rolandGarrosMen2025FinalMatch());

            redisTemplate.opsForValue().set("live:atp:1", match1Str);
            redisTemplate.opsForValue().set("live:atp:2", match3Str);

            // 2. liveMatch에 1번 데이터만 전송
            liveMatchService.updateLiveMatches();
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }

       verify(eventPublisher).publishEvent(
               argThat((Object event) ->
                       event instanceof MatchFinishedEvent &&
                               ((MatchFinishedEvent) event).rapidMatchId().equals("2")

               )
       );

    }

    @Test
    void ATP_경기_조회(){
        List<LiveMatchResponse> res = null;
        try{
            LiveMatchResponse response1 = LiveEventsFixtures.liveMatchInProgress1();
            LiveMatchResponse response2 = LiveEventsFixtures.liveMatchInProgress3();

            String response1Str = objectMapper.writeValueAsString(response1);
            String response2Str = objectMapper.writeValueAsString(response2);

            redisTemplate.opsForValue().set("live:atp:1", response1Str);
            redisTemplate.opsForValue().set("live:atp:2", response2Str);

            res = liveMatchService.getATPLiveEventsByRedis();
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }


        assertThat(res).hasSize(2);
        assertThat(res)
                .extracting(LiveMatchResponse::getRapidId, LiveMatchResponse::getTournamentName, LiveMatchResponse::getRoundName,
                        p -> p.getHomeScore().getCurrent(), p -> p.getAwayScore().getCurrent())
                .containsExactlyInAnyOrder(
                        tuple("1", "Wimbledon","Final", 1L, 1L),
                        tuple("2", "Wimbledon", "Final", 1L, 1L)
                );

    }

    @Test
    void ATP_경기_단일_조회(){
        LiveMatchResponse res = null;
        try{
            LiveMatchResponse response1 = LiveEventsFixtures.liveMatchInProgress1();
            String response1Str = objectMapper.writeValueAsString(response1);
            redisTemplate.opsForValue().set("live:atp:1", response1Str);
            redisTemplate.opsForValue().set("index:rapidId:1", response1Str);

            res = liveMatchService.getLiveEventByRedis("1");
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }


        assertThat(res.getRapidId()).isEqualTo("1");
        assertThat(res.getRoundName()).isEqualTo("Final");
        assertThat(res.getSeasonName()).isEqualTo("Wimbledon Men Singles 2025");

    }

    @Test
    void Davis_Cup_경기_조회(){
        List<LiveMatchResponse> res = null;
        try{
            LiveMatchResponse laverCup = EtcLiveEventsFixtures.etcLiveMatchInProgress1();
            LiveMatchResponse unitedCup = EtcLiveEventsFixtures.etcLiveMatchInProgress2();
            LiveMatchResponse response1 = LiveEventsFixtures.liveMatchInProgress1();

            String laverCupStr = objectMapper.writeValueAsString(laverCup);
            String unitedCupStr = objectMapper.writeValueAsString(unitedCup);
            String response1Str = objectMapper.writeValueAsString(response1);
            redisTemplate.opsForValue().set("live:"+ CategoryType.EXHIBITION.getCategorySlug()+laverCup.getRapidId(), laverCupStr);
            redisTemplate.opsForValue().set("live:"+ CategoryType.UNITED_CUP.getCategorySlug()+unitedCup.getRapidId(), unitedCupStr);
            redisTemplate.opsForValue().set("live:atp:1", response1Str);

            res = liveMatchService.getEtcLiveEventsByRedis();
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }

        assertThat(res).hasSize(2);
        assertThat(res)
                .extracting(LiveMatchResponse::getRapidId, LiveMatchResponse::getTournamentName, LiveMatchResponse::getRoundName,
                        p -> p.getHomeScore().getCurrent(), p -> p.getAwayScore().getCurrent())
                .containsExactlyInAnyOrder(
                        tuple("5", "Laver Cup","Semifinals", 1L, 1L),
                        tuple("6", "United Cup", "UNKNOWN", 1L, 1L)
                );

    }

    @Test
    void 라이브_경기_요약_조회() {
        List<LiveMatchSummaryResponse> res = null;
        try {
            LiveMatchResponse laverCup = EtcLiveEventsFixtures.etcLiveMatchInProgress1();
            LiveMatchResponse unitedCup = EtcLiveEventsFixtures.etcLiveMatchInProgress2();
            LiveMatchResponse wimbledon = LiveEventsFixtures.liveMatchInProgress1();

            String laverCupStr = objectMapper.writeValueAsString(laverCup);
            String unitedCupStr = objectMapper.writeValueAsString(unitedCup);
            String wimbledonStr = objectMapper.writeValueAsString(wimbledon);

            redisTemplate.opsForValue().set("index:rapidId:" + laverCup.getRapidId(), laverCupStr);
            redisTemplate.opsForValue().set("index:rapidId:" + unitedCup.getRapidId(), unitedCupStr);
            redisTemplate.opsForValue().set("index:rapidId:" + wimbledon.getRapidId(), wimbledonStr);

            res = liveMatchService.getLiveEventsSummary();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        assertThat(res).hasSize(3);
        assertThat(res)
                .extracting(r -> r.getHomePlayer().getPlayerRapidId(),
                        r -> r.getHomePlayer().getPlayerName(),
                        r -> r.getHomePlayer().getPlayerRanking(),
                        r -> r.getAwayPlayer().getPlayerRapidId(),
                        r -> r.getAwayPlayer().getPlayerName(),
                        r -> r.getAwayPlayer().getPlayerRanking(),
                        LiveMatchSummaryResponse::getHomeScore,
                        LiveMatchSummaryResponse::getAwayScore,
                        LiveMatchSummaryResponse::getCategory,
                        LiveMatchSummaryResponse::getTournamentName,
                        LiveMatchSummaryResponse::getSeasonName,
                        LiveMatchSummaryResponse::getRoundName)
                .containsExactlyInAnyOrder(
                        tuple("275923", "Alcaraz", "1", "206570", "Jannik Sinner", "2", 1L, 1L, "exhibition", "Laver Cup", "Exhibition Laver Cup Men Singles 2025", "Semifinals"),
                        tuple("275923", "Alcaraz", "1", "206570", "Jannik Sinner", "2", 1L, 1L, "united-cup", "United Cup", "United Cup 2025", "UNKNOWN"),
                        tuple("275923", "Alcaraz", "1", "206570", "Jannik Sinner", "2", 1L, 1L, "atp", "Wimbledon", "Wimbledon Men Singles 2025", "Final")
                );
    }

    @Test
    void 라이브_경기와_DB_시작_시간이_다른_경우_DB_업데이트(){

        try{
            matchRepository.save(Match.builder()
                    .matchId(1L)
                    .rapidMatchId("1")
                    .startTimestamp("20250701090000")
                    .status("3rd Set")
                    .build());

            LiveMatchResponse response1 = LiveEventsFixtures.liveMatchInProgress1();
            String response1Str = objectMapper.writeValueAsString(response1);
            redisTemplate.opsForValue().set("live:atp:1", response1Str);
            redisTemplate.opsForValue().set("index:rapidId:1", response1Str);

            liveMatchService.updateLiveMatches();


        }catch(JsonProcessingException e){
            e.printStackTrace();
        }

        verify(eventPublisher).publishEvent(
                argThat((Object event) ->
                        event instanceof MatchStartTimeChangedEvent &&
                                ((MatchStartTimeChangedEvent) event).rapidMatchId().equals("1")

                )
        );

    }
}
