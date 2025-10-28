package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.infrastructure.apiCall.ApiCallEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiCallJpaRepository extends JpaRepository<ApiCallEntity, Long> {
}
