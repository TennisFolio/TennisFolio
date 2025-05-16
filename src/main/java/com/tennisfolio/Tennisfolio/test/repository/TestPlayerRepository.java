package com.tennisfolio.Tennisfolio.test.repository;

import com.tennisfolio.Tennisfolio.test.domain.TestPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestPlayerRepository extends JpaRepository<TestPlayer, Long>{
}
