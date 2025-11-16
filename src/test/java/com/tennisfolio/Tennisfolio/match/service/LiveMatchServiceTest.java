package com.tennisfolio.Tennisfolio.match.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennisfolio.Tennisfolio.fixtures.LiveEventsFixtures;
import com.tennisfolio.Tennisfolio.fixtures.MatchFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.event.EventTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.LiveEventsTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.match.application.LiveMatchService;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakeMatchRepository;
import com.tennisfolio.Tennisfolio.mock.FakePlayerRepository;
import com.tennisfolio.Tennisfolio.mock.FakeRedisTemplate;
import com.tennisfolio.Tennisfolio.mock.event.FakeEventMapper;
import com.tennisfolio.Tennisfolio.mock.liveEvents.FakeLiveEventsMapper;
import com.tennisfolio.Tennisfolio.mock.liveEvents.FakeLiveEventsMapperSecond;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.when;

public class LiveMatchServiceTest {
    LiveMatchService liveMatchService;
    ApiWorker apiWorker;
    @Mock
    PlayerProvider playerProvider;

    ObjectMapper objectMapper = new ObjectMapper();

    RedisTemplate redisTemplate = new FakeRedisTemplate();
    MatchRepository matchRepository = new FakeMatchRepository();

    private List<StrategyApiTemplate<?, ?>> strategies = new ArrayList<>();
    private ApiCaller fakeApiCaller = new FakeApiCaller();
    @Mock
    private ResponseParser parser;
    @Mock
    private ApiCallCounter apiCallCounter;

    @Mock
    PlayerImageService playerImageService;

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
        apiWorker = new ApiWorker(strategies);

        liveMatchService = new LiveMatchService(apiWorker, playerProvider, redisTemplate, matchRepository);
    }

    @Test
    void 처음_경기_생성시_redis_데이터_추가(){

        liveMatchService.updateLiveMatches();

        LiveMatchResponse res = (LiveMatchResponse)redisTemplate.opsForValue().get("live:atp:1");

        assertThat(res.getHomeScore().getPoint()).isEqualTo("40");
        assertThat(res.getAwayScore().getPoint()).isEqualTo("40");

    }

    @Test
    void 데이터_변경_시_redis_데이터_수정(){
        // 첫번째 호출로 progress1 수정
        // redis 수정 -> redis 조회
        LiveMatchResponse response1 = LiveEventsFixtures.liveMatchInProgress1();
        redisTemplate.opsForValue().set("live:atp:1", response1);
        // 두번째 호출로 progress2로 수정
        // redis 수정 -> redis 조회
        liveMatchService.updateLiveMatches();

        LiveMatchResponse res = (LiveMatchResponse)redisTemplate.opsForValue().get("live:atp:1");

        assertThat(res.getHomeScore().getPoint()).isEqualTo("40");
        assertThat(res.getAwayScore().getPoint()).isEqualTo("40");

    }

    @Test
    void 경기_종료_시_데이터_업데이트(){
        // 1. redis에 1,2번 경기 데이터 추가
        LiveMatchResponse match1 = LiveEventsFixtures.liveMatchInProgress1();
        LiveMatchResponse match3 = LiveEventsFixtures.liveMatchInProgress3();
        matchRepository.save(MatchFixtures.wimbledonMen2025FinalMatch());
        matchRepository.save(MatchFixtures.rolandGarrosMen2025FinalMatch());

        redisTemplate.opsForValue().set("live:atp:1", match1);
        redisTemplate.opsForValue().set("live:atp:2", match3);

        // 2. liveMatch에 1번 데이터만 전송
        liveMatchService.updateLiveMatches();

        // 3. match3은 DB에 업데이트
        Match res = matchRepository.findByRapidMatchId("2").get();

        assertThat(res).isNotNull();
        assertThat(res.getRapidMatchId()).isEqualTo("2");

    }

    @Test
    void ATP_경기_조회(){
        LiveMatchResponse response1 = LiveEventsFixtures.liveMatchInProgress1();
        LiveMatchResponse response2 = LiveEventsFixtures.liveMatchInProgress3();
        redisTemplate.opsForValue().set("live:atp:1", response1);
        redisTemplate.opsForValue().set("live:atp:2", response2);

        List<LiveMatchResponse> res = liveMatchService.getATPLiveEventsByRedis();

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
        LiveMatchResponse response1 = LiveEventsFixtures.liveMatchInProgress1();

        redisTemplate.opsForValue().set("live:atp:1", response1);
        redisTemplate.opsForValue().set("index:rapidId:1", response1);

        LiveMatchResponse res = liveMatchService.getLiveEventByRedis("1");

        assertThat(res.getRapidId()).isEqualTo("1");
        assertThat(res.getRoundName()).isEqualTo("Final");
        assertThat(res.getSeasonName()).isEqualTo("Wimbledon Men Singles 2025");

    }
}
