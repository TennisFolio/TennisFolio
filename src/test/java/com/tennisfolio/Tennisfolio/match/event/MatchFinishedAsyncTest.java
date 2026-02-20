package com.tennisfolio.Tennisfolio.match.event;



import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiCaller;
import com.tennisfolio.Tennisfolio.match.application.LiveMatchService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@EnableAsync
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class MatchFinishedAsyncTest {

    @MockitoBean
    @Qualifier("fakeApiCaller")
    ApiCaller apiCaller;

    @Autowired
    ApplicationEventPublisher publisher;

    @MockitoSpyBean
    LiveMatchService liveMatchService;

    @Test
    @Transactional
    void 이벤트는_비동기로_실행된다() throws Exception {
        // given
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();

        doAnswer(invocation -> {
            threadName.set(Thread.currentThread().getName());
            latch.countDown();
            return null;
        }).when(liveMatchService).finishMatchProc(anyString());

        // when
        publisher.publishEvent(new MatchFinishedEvent("1"));

        TestTransaction.flagForCommit();
        TestTransaction.end();
        // then
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertThat(threadName.get()).startsWith("event-");
    }
}
