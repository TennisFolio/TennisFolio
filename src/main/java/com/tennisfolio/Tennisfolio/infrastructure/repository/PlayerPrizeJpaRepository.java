package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.prize.repository.PlayerPrizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerPrizeJpaRepository extends JpaRepository<PlayerPrizeEntity, Long> {
}
