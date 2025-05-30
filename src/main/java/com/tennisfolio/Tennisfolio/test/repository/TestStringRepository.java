package com.tennisfolio.Tennisfolio.test.repository;

import com.tennisfolio.Tennisfolio.test.domain.TestPlayer;
import com.tennisfolio.Tennisfolio.test.domain.TestString;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestStringRepository extends JpaRepository<TestString, Long> {
    Optional<TestString> findByQuery(String query);
}
