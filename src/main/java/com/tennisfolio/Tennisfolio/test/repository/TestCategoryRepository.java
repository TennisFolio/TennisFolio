package com.tennisfolio.Tennisfolio.test.repository;

import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.test.domain.TestCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCategoryRepository extends JpaRepository<TestCategory, Long> {
}
