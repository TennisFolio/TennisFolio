package com.tennisfolio.Tennisfolio.infrastructure.worker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenericBatchWorkerTest {
    @Mock
    private BatchSaver<Integer> saver;

    private GenericBatchWorker<Integer> worker;

    @BeforeEach
    void setUp() {
        // batchLimit = 5, queueCapacity = 50
        worker = new GenericBatchWorker<>(saver, 5, 50);
    }

    @Test
    void submit하면_Consumer가_flush까지_자동실행된다() throws Exception {

        // latch로 flush 발생 감지
        CountDownLatch latch = new CountDownLatch(1);

        // saveBatch() 호출 시 latch 감소
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(saver).saveBatch(anyList());

        // ---- 테스트 동작 ----
        // flush 조건: 5개 데이터 → 5번 submit
        for (int i = 1; i <= 5; i++) {
            worker.submit(List.of(i));
        }

        // ---- 검증 ----
        assertTrue(latch.await(2, TimeUnit.SECONDS),
                "flush(saveBatch)가 일정 시간 내에 호출되지 않았습니다.");

        // saveBatch는 정확히 한 번 호출되며
        // 내부 리스트 사이즈도 5여야 한다.
        verify(saver, times(1)).saveBatch(argThat(list ->
                list.size() == 5 &&
                        list.containsAll(List.of(1, 2, 3, 4, 5))
        ));
    }



    @Test
    void shutdown하면_남은_데이터도_flush된다() throws Exception {

        CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(saver).saveBatch(anyList());

        // batchLimit = 5인데 3개만 넣음 → flush 안된 상태
        worker.submit(List.of(10));
        worker.submit(List.of(20));
        worker.submit(List.of(30));

        // shutdown 시 남은 buffer를 flush 해야 한다
        worker.shutdownAndAwait(2, TimeUnit.SECONDS);

        assertTrue(latch.await(2, TimeUnit.SECONDS),
                "shutdown 시 flush가 호출되지 않았습니다.");

        verify(saver).saveBatch(argThat(list ->
                list.size() == 3 &&
                        list.containsAll(List.of(10, 20, 30))
        ));
    }



    @Test
    void 여러_submit이_여러번_flush를_발생시킨다() throws Exception {

        CountDownLatch latch = new CountDownLatch(2); // flush 2번 예상

        doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(saver).saveBatch(anyList());

        // 총 10개 → batchLimit 5 → flush 2번
        for (int i = 1; i <= 10; i++) {
            worker.submit(List.of(i));
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS),
                "두 번의 flush가 호출되지 않았습니다.");

        verify(saver, times(2)).saveBatch(anyList());
    }
}
