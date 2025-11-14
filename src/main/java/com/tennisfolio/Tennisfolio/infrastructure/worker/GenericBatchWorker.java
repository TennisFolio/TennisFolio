package com.tennisfolio.Tennisfolio.infrastructure.worker;

import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class GenericBatchWorker<T> {

    private final LinkedBlockingQueue<List<T>> queue = new LinkedBlockingQueue<>();
    private final ExecutorService consumerExecutor = Executors.newSingleThreadExecutor();

    private final BatchSaver<T> saver;
    private final int batchLimit;

    private final List<T> buffer = new ArrayList<>();
    private volatile boolean running = true;

    public GenericBatchWorker(BatchSaver<T> saver, int batchLimit){
        this.saver = saver;
        this.batchLimit = batchLimit;
        start();
    }
    private void start(){
        consumerExecutor.submit(() -> {
            while(running || !queue.isEmpty()){
                try{
                    List<T> items = queue.poll(1, TimeUnit.SECONDS);
                    if(items == null) continue;

                    buffer.addAll(items);

                    if(buffer.size() >= batchLimit){
                        flush();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            flush();
        });
    }

    private void flush(){
        if(buffer.isEmpty()) return;

        try{
            saver.saveBatch(buffer);
            buffer.clear();
        }catch(Exception e){
            e.printStackTrace();
            queue.offer(new ArrayList<>(buffer));
            buffer.clear();
        }
    }

    public void submit(List<T> items){
        if(items == null || items.isEmpty()) return;
        try{
            queue.put(items);
        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    public void shutdown(){
        running = false;
        consumerExecutor.shutdown();
    }
}
