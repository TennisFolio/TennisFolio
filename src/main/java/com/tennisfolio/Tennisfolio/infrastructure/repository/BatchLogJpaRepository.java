package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.infrastructure.batchlog.BatchLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchLogJpaRepository extends JpaRepository<BatchLogEntity, Long> {
}
