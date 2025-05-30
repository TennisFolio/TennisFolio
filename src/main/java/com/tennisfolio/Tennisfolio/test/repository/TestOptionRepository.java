package com.tennisfolio.Tennisfolio.test.repository;

import com.tennisfolio.Tennisfolio.test.domain.TestOption;
import com.tennisfolio.Tennisfolio.test.domain.TestOptionMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestOptionRepository extends JpaRepository<TestOption, Long> {
    List<TestOption> findByOptionIdIn(List<Long> optionIdList);
}
