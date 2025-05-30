package com.tennisfolio.Tennisfolio.test.repository;

import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.test.domain.TestCategory;
import com.tennisfolio.Tennisfolio.test.domain.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {
    List<TestQuestion> findByTestCategory(TestCategory testCategory);
}
