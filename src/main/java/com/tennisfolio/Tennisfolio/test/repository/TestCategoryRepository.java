package com.tennisfolio.Tennisfolio.test.repository;

import com.tennisfolio.Tennisfolio.common.TestType;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.test.domain.TestCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestCategoryRepository extends JpaRepository<TestCategory, Long> {
    Optional<TestCategory> findByTestType(TestType TestType);
}
