package com.tennisfolio.Tennisfolio.infrastructure.batchlog;

import com.tennisfolio.Tennisfolio.infrastructure.repository.BatchLogJpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BatchExecutor {
    private final BatchLogJpaRepository batchLogJpaRepository;

    public BatchExecutor(BatchLogJpaRepository batchLogJpaRepository) {
        this.batchLogJpaRepository = batchLogJpaRepository;
    }

    public void run(String batchName, Runnable task){

        BatchLogEntity log = batchLogJpaRepository.save(BatchLogEntity.builder()
                .batchName(batchName)
                .startTime(LocalDateTime.now())
                .status("RUNNING")
                .build());

        try{
            task.run();
            log.markSuccess(LocalDateTime.now());
        }catch(Exception e){
            log.markFail(LocalDateTime.now(), e.getMessage());
        }finally{
            batchLogJpaRepository.save(log);
        }
    }
}
