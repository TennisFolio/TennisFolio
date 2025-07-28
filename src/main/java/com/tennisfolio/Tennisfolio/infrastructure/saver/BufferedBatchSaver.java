package com.tennisfolio.Tennisfolio.infrastructure.saver;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

public class BufferedBatchSaver<E> {


    private JpaRepository<E, Long> jpaRepository;
    private int batchSize;
    private List<E> buffer = new ArrayList<>();
    private TransactionTemplate transactionTemplate;

    public BufferedBatchSaver(JpaRepository jpaRepository, int batchSize,  TransactionTemplate transactionTemplate){
        this.jpaRepository = jpaRepository;
        this.batchSize = batchSize;
        this.transactionTemplate = transactionTemplate;
    }

    // 엔티티 리스트 넣기
    public List<E> collect(List<E> entities){
        add(entities);
        return entities;
    }
    
    // 1개 엔티티 넣기
    public List<E> collect(E entity){
        buffer.add(entity);
        return List.of(entity);
    }
    // 다 찼을 때 저장
    public boolean flushWhenFull() {
        if (isFull()) {
            flushBuffer();
            return true;
        }
        return false;
    }

    // batchSize와 별개로 저장
    public boolean flushAll() {
        if (!buffer.isEmpty()) {
            flushBuffer();
            return true;
        }
        return false;
    }

    private void add(List<E> entities){
        this.buffer.addAll(entities);
    }

    private boolean isFull(){
        return this.buffer.size() >= this.batchSize;
    }

    private List<E> flushBuffer(){
        List<E> save = transactionTemplate.execute(status -> {
            List<E> result = jpaRepository.saveAll(buffer);
            return result;
        });

        buffer.clear();

        return save;
    }
}
