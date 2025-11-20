package com.tennisfolio.Tennisfolio.infrastructure.worker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class GenericBatchWorker<T> {

    private final LinkedBlockingQueue<List<T>> queue;
    private final ExecutorService consumerExecutor = Executors.newSingleThreadExecutor();

    private final BatchSaver<T> saver;
    private final int batchLimit;

    private final List<T> buffer = new ArrayList<>();
    private final Object bufferLock = new Object();
    private volatile boolean running = true;
    private final MeterRegistry meterRegistry;
    private final Timer flushTimer;
    private final Counter processedCounter;
    private final Counter errorCounter;

    public GenericBatchWorker(BatchSaver<T> saver, int batchLimit, int queueCapacity, MeterRegistry meterRegistry){
        this.saver = saver;
        this.batchLimit = batchLimit;
        this.queue = new LinkedBlockingQueue<>(queueCapacity);
        this.meterRegistry = meterRegistry;
        this.flushTimer = meterRegistry.timer("batch_worker_flush_duration_seconds");
        this.processedCounter = meterRegistry.counter("batch_worker_processed_matches_total");
        this.errorCounter = meterRegistry.counter("batch_worker_errors_total");
        registerMetrics(meterRegistry);
        start();
    }
    private void start(){
        consumerExecutor.submit(() -> {
            while(running || !queue.isEmpty()){
                try{
                    while(running || !queue.isEmpty()){
                        List<T> items = queue.poll(1, TimeUnit.SECONDS);
                        if(items == null || items.isEmpty()){
                            if(!running && queue.isEmpty()){
                                flush();
                                break;
                            }
                            continue;
                        }

                        synchronized(bufferLock){
                            buffer.addAll(items);
                            if(buffer.size() >= batchLimit){
                                flush();
                            }
                        }
                    }

                    synchronized (bufferLock){
                        flush();
                    }
                } catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void flush(){
        if(buffer.isEmpty()) return;

        List<T> toSave = new ArrayList<>(buffer);
        buffer.clear();
        flushTimer.record(() -> {
            try{
                saver.saveBatch(toSave);
                processedCounter.increment(toSave.size());
            }catch(Exception e){
                errorCounter.increment();
                System.err.println("[GenericBatchWorker] Failed to save batch, size=" + toSave.size());
                e.printStackTrace();
            }
        });

    }

    public void submit(List<T> items){
        if(items == null || items.isEmpty()) return;
        List<T> copy = new ArrayList<>(items);
        try{
            queue.put(copy);
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while submitting batch", e);
        }
    }

    public int getPendingCount() {
        synchronized (bufferLock) {
            return buffer.size() + queue.stream().mapToInt(List::size).sum();
        }
    }

    public void awaitCapacity(int maxPending, long checkIntervalMillis) {
        try {
            while (true) {
                int pending = getPendingCount();
                if (pending <= maxPending) {
                    return;
                }
                Thread.sleep(checkIntervalMillis);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for capacity", e);
        }
    }


    public void shutdownAndAwait(long timeout, TimeUnit unit) {
        running = false;
        consumerExecutor.shutdown();
        try {
            if (!consumerExecutor.awaitTermination(timeout, unit)) {
                consumerExecutor.shutdownNow();
            }
            synchronized (bufferLock) {
                flush();
            }
        } catch (InterruptedException e) {
            consumerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void registerMetrics(MeterRegistry registry) {
        Gauge.builder("batch_worker_queue_size", queue, LinkedBlockingQueue::size)
                .description("Current queue size of GenericBatchWorker")
                .register(registry);

        Gauge.builder("batch_worker_buffer_size", this, w -> w.buffer.size())
                .description("Buffered items before flush")
                .register(registry);
    }
}
