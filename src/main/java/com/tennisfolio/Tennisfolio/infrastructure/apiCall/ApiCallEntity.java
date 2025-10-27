package com.tennisfolio.Tennisfolio.infrastructure.apiCall;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tb_api_call")
@Getter
@NoArgsConstructor
public class ApiCallEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="API_CALL_ID")
    private Long apiCallId;
    @Column(name="API_DATE")
    private String apiDate;
    @Column(name="API_NAME")
    private String apiName;
    @Column(name="API_COUNT")
    private Long apiCount;

    @Builder
    public ApiCallEntity(String apiDate, String apiName, Long apiCount){
        this.apiDate = apiDate;
        this.apiName = apiName;
        this.apiCount = apiCount;
    }
}
