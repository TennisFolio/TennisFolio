package com.tennisfolio.Tennisfolio.infrastructure.batchlog;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_batch_log")
@Getter
@NoArgsConstructor
public class BatchLogEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="BATCH_ID")
    private Long id;
    @Column(name="BATCH_NAME")
    private String batchName;
    @Column(name="START_TIME")
    private LocalDateTime startTime;
    @Column(name="END_TIME")
    private LocalDateTime endTime;
    @Column(name="STATUS")
    private String status;
    @Column(name="DURATION_MS")
    private Long durationMs;
    @Column(name="MESSAGE", columnDefinition = "TEXT")
    private String message;

    @Builder
    public BatchLogEntity(String batchName, LocalDateTime startTime, String status, String message){
        this.batchName = batchName;
        this.startTime = startTime;
        this.status = status;
        this.message = message;
    }

    public void markSuccess(LocalDateTime endTime){
        this.status = "SUCCESS";
        this.endTime = endTime;
        this.durationMs = Duration.between(startTime, endTime).toMillis();
    }

    public void markFail(LocalDateTime endTime, String message){
        this.status = "FAIL";
        this.endTime = endTime;
        this.message = message;
        this.durationMs = Duration.between(startTime, endTime).toMillis();
    }


}
