package com.tennisfolio.Tennisfolio.match.service;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.LiveEventsTemplate;
import com.tennisfolio.Tennisfolio.match.application.LiveMatchService;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.mock.FakeApiCaller;
import com.tennisfolio.Tennisfolio.mock.FakeMatchRepository;
import com.tennisfolio.Tennisfolio.mock.liveEvents.FakeLiveEventsMapper;
import com.tennisfolio.Tennisfolio.mock.liveEvents.FakeLiveEventsMapperSecond;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

public class LiveMatchServiceTest {
    LiveMatchService liveMatchService;
    ApiWorker apiWorker;
    PlayerProvider playerProvider;
    @Autowired
    RedisTemplate redisTemplate;
    MatchRepository matchRepository = new FakeMatchRepository();
    private List<StrategyApiTemplate<?, ?>> strategies = new ArrayList<>();
    private ApiCaller fakeApiCaller = new FakeApiCaller();
    @Mock
    private ResponseParser parser;
    @Mock
    private ApiCallCounter apiCallCounter;


    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        FakeLiveEventsMapper fakeLiveEventsMapper = new FakeLiveEventsMapper();
        FakeLiveEventsMapperSecond fakeLiveEventsMapperSecond = new FakeLiveEventsMapperSecond();
        LiveEventsTemplate liveEventsTemplate = new LiveEventsTemplate(fakeApiCaller, parser, fakeLiveEventsMapper, apiCallCounter);

        LiveEventsTemplate liveEventsTemplateSecond = new LiveEventsTemplate(fakeApiCaller, parser, fakeLiveEventsMapperSecond, apiCallCounter);

        strategies.addAll(List.of(liveEventsTemplate, liveEventsTemplateSecond));
    }

    @Test
    void 데이터_변경_시_redis_데이터_수정(){
        // 첫번째 호출로 progress1 수정
        // redis 수정 -> redis 조회

        // 두번째 호출로 progress2로 수정
        // redis 수정 -> redis 조회

    }
}
