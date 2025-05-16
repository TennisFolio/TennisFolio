package com.tennisfolio.Tennisfolio.test.repository;

import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.test.domain.TestOptionMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestOptionMappingRepository extends JpaRepository<TestOptionMapping, Long> {
}
