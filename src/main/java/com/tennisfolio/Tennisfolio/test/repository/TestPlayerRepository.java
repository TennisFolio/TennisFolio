package com.tennisfolio.Tennisfolio.test.repository;

import com.tennisfolio.Tennisfolio.test.domain.model.TestPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestPlayerRepository extends JpaRepository<TestPlayer, Long>{
    Optional<TestPlayer> findByQuery(String query);
}
