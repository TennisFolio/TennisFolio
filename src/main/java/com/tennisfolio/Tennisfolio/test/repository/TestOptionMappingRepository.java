package com.tennisfolio.Tennisfolio.test.repository;

import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.test.domain.TestOption;
import com.tennisfolio.Tennisfolio.test.domain.TestOptionMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestOptionMappingRepository extends JpaRepository<TestOptionMapping, Long> {
    List<TestOptionMapping> findByOptionIn(List<TestOption> optionList);
}
