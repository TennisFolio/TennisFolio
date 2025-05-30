package com.tennisfolio.Tennisfolio.test.repository;

import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.test.domain.TestPlayer;
import com.tennisfolio.Tennisfolio.test.domain.TestRacket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestRacketRepository extends JpaRepository<TestRacket, Long> {
    Optional<TestRacket> findByQuery(String query);
}
