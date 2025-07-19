package com.tennisfolio.Tennisfolio.infrastructure.saver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

public class BufferedBatchSaver<E> {
    private JpaRepository<E, Long> jpaRepository;
    private int batchSize;
    private List<E> buffer = new ArrayList<>();

    public BufferedBatchSaver(JpaRepository jpaRepository, int batchSize){
        this.jpaRepository = jpaRepository;
        this.batchSize = batchSize;
    }

    // 엔티티 리스트 넣고 저장
    public List<E> collect(List<E> entities){
        add(entities);
        if(isFull()) return flushBuffer();
        return entities;
    }
    
    // 1개 엔티티 넣고 저장
    public List<E> collect(E entity){
        buffer.add(entity);
        if(isFull()) return buffer;
        return List.of(entity);
    }

    // 다 안찼지만 저장이 필요할 때 
    public List<E> flush() {
        if (!buffer.isEmpty()) {
            return flushBuffer();
        }
        return List.of();
    }

    private void add(List<E> entities){
        this.buffer.addAll(entities);
    }

    private boolean isFull(){
        return this.buffer.size() >= this.batchSize;
    }

    private List<E> flushBuffer(){
        List<E> save = jpaRepository.saveAll(buffer);
        buffer.clear();

        return save;
    }
}
