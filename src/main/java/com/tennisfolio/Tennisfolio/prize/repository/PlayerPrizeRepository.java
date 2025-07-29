package com.tennisfolio.Tennisfolio.prize.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerPrizeRepository extends JpaRepository<PlayerPrizeEntity, Long> {
}
