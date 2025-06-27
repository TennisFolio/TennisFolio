package com.tennisfolio.Tennisfolio.test.repository;

import com.tennisfolio.Tennisfolio.test.domain.model.TestString;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestStringRepository extends JpaRepository<TestString, Long> {
    Optional<TestString> findByQuery(String query);
}
